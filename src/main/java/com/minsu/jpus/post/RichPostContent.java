package com.minsu.jpus.post;

import com.minsu.jpus.post.dto.PostImageResponse;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Component
public class RichPostContent {

  public static final int MAX_IMAGES = 5;
  public static final int MAX_TEXT_LENGTH = 20_000;
  private static final int MAX_DOCUMENT_BYTES = 256 * 1024;
  private static final int MAX_DEPTH = 32;
  private static final int MAX_NODES = 10_000;
  private static final Pattern CLIENT_ID = Pattern.compile("[A-Za-z0-9_-]{1,80}");
  private static final Set<String> BLOCK_NODES = Set.of(
      "paragraph", "heading", "bulletList", "orderedList", "blockquote",
      "codeBlock", "horizontalRule", "image"
  );
  private static final Set<String> MARKS = Set.of(
      "bold", "italic", "strike", "underline", "code"
  );

  private final JsonMapper mapper;

  public RichPostContent(JsonMapper mapper) {
    this.mapper = mapper;
  }

  public record ValidatedContent(
      ObjectNode document,
      List<String> imageClientIds,
      String plainText
  ) {
  }

  public ValidatedContent validate(JsonNode document, List<String> imageClientIds) {
    List<String> ids = imageClientIds == null ? List.of() : imageClientIds;
    if (ids.size() > MAX_IMAGES) {
      throw invalid("이미지는 최대 5개까지 첨부할 수 있습니다.");
    }
    Set<String> expectedIds = new LinkedHashSet<>();
    for (String id : ids) {
      if (id == null || !CLIENT_ID.matcher(id).matches() || !expectedIds.add(id)) {
        throw invalid("이미지 식별자가 잘못되었거나 중복되었습니다.");
      }
    }
    if (document == null || !document.isObject()) {
      throw invalid("본문은 doc 형식의 JSON 객체여야 합니다.");
    }
    if (mapper.writeValueAsBytes(document).length > MAX_DOCUMENT_BYTES) {
      throw invalid("본문 문서는 256KiB 이하여야 합니다.");
    }

    ValidationState state = new ValidationState();
    ObjectNode normalized = normalize(document, null, 0, state, 0);
    if (!state.imageIds.equals(expectedIds)) {
      throw invalid("본문 이미지 참조와 첨부 파일 목록이 일치하지 않습니다.");
    }
    String plainText = state.plainText.toString().strip();
    if (plainText.length() > MAX_TEXT_LENGTH) {
      throw invalid("본문은 20,000자 이하여야 합니다.");
    }
    if (plainText.isBlank() && expectedIds.isEmpty()) {
      throw invalid("본문이나 이미지를 입력해 주세요.");
    }
    return new ValidatedContent(normalized, List.copyOf(ids), plainText);
  }

  public String attachImages(ValidatedContent content, Map<String, Long> imageIds) {
    if (!imageIds.keySet().equals(new HashSet<>(content.imageClientIds()))) {
      throw new IllegalStateException("저장한 이미지 목록이 본문 참조와 일치하지 않습니다.");
    }
    ObjectNode document = content.document().deepCopy();
    replaceImageReferences(document, imageIds);
    return mapper.writeValueAsString(document);
  }

  public JsonNode forResponse(String storedDocument, Long postId, Set<Long> imageIds) {
    if (storedDocument == null) {
      return null;
    }
    JsonNode document = mapper.readTree(storedDocument);
    if (document == null || !document.isObject()) {
      throw new IllegalStateException("저장된 본문 문서가 올바르지 않습니다.");
    }
    addImageUrls(document, postId, imageIds);
    return document;
  }

  private ObjectNode normalize(
      JsonNode node,
      String parent,
      int index,
      ValidationState state,
      int depth
  ) {
    if (depth > MAX_DEPTH || ++state.nodeCount > MAX_NODES || !node.isObject()) {
      throw invalid("본문의 구조가 잘못되었거나 너무 복잡합니다.");
    }
    requireKeys(node, Set.of("type", "attrs", "content", "text", "marks"));
    String type = requiredString(node, "type");
    if (!allowedChild(parent, type, index)) {
      throw invalid("지원하지 않는 본문 구조입니다: " + type);
    }

    ObjectNode normalized = mapper.createObjectNode();
    normalized.put("type", type);
    JsonNode attrs = node.get("attrs");
    if (attrs != null && !attrs.isNull() && !attrs.isObject()) {
      throw invalid("본문 속성은 객체여야 합니다.");
    }
    normalizeAttributes(type, attrs, normalized, state);

    if (type.equals("text")) {
      String text = requiredString(node, "text");
      if (text.isEmpty() || node.has("content")) {
        throw invalid("텍스트 노드가 올바르지 않습니다.");
      }
      normalized.put("text", text);
      appendText(state, text);
    } else if (node.has("text")) {
      throw invalid("텍스트 속성은 text 노드에만 사용할 수 있습니다.");
    }
    normalizeMarks(node, type, parent, normalized);

    JsonNode children = node.get("content");
    if (children != null && !children.isArray()) {
      throw invalid("본문 content는 배열이어야 합니다.");
    }
    int childCount = children == null ? 0 : children.size();
    if (Set.of("doc", "bulletList", "orderedList", "listItem", "blockquote")
        .contains(type) && childCount == 0) {
      throw invalid("본문 블록에 필요한 내용이 없습니다.");
    }
    if (childCount > 0) {
      ArrayNode normalizedChildren = normalized.putArray("content");
      for (int i = 0; i < childCount; i++) {
        normalizedChildren.add(normalize(children.get(i), type, i, state, depth + 1));
      }
    }
    if (type.equals("hardBreak") || BLOCK_NODES.contains(type)
        || type.equals("listItem")) {
      appendText(state, "\n");
    }
    return normalized;
  }

  private boolean allowedChild(String parent, String type, int index) {
    if (parent == null) return type.equals("doc");
    return switch (parent) {
      case "doc", "blockquote" -> BLOCK_NODES.contains(type);
      case "bulletList", "orderedList" -> type.equals("listItem");
      case "listItem" -> index == 0 ? type.equals("paragraph") : BLOCK_NODES.contains(type);
      case "paragraph", "heading" -> type.equals("text") || type.equals("hardBreak");
      case "codeBlock" -> type.equals("text");
      default -> false;
    };
  }

  private void normalizeAttributes(
      String type,
      JsonNode attrs,
      ObjectNode normalized,
      ValidationState state
  ) {
    if (type.equals("image")) {
      if (attrs == null || !attrs.isObject()) throw invalid("이미지 참조가 없습니다.");
      requireKeys(attrs, Set.of("clientId", "alt", "title"));
      String clientId = requiredString(attrs, "clientId");
      if (!CLIENT_ID.matcher(clientId).matches()) throw invalid("이미지 식별자가 잘못되었습니다.");
      state.imageIds.add(clientId);
      ObjectNode target = normalized.putObject("attrs");
      target.put("clientId", clientId);
      copyOptionalString(attrs, target, "alt", 500);
      copyOptionalString(attrs, target, "title", 500);
    } else if (type.equals("heading")) {
      if (attrs == null || !attrs.isObject()) throw invalid("제목 단계가 없습니다.");
      requireKeys(attrs, Set.of("level"));
      JsonNode level = attrs.get("level");
      if (level == null || !level.isIntegralNumber() || !level.canConvertToInt()
          || (level.intValue() != 2 && level.intValue() != 3)) {
        throw invalid("본문 제목은 2단계 또는 3단계만 사용할 수 있습니다.");
      }
      normalized.putObject("attrs").put("level", level.intValue());
    } else if (type.equals("orderedList")) {
      if (attrs != null && !attrs.isNull()) {
        requireKeys(attrs, Set.of("start", "type"));
        JsonNode start = attrs.get("start");
        if (start != null && (!start.isIntegralNumber() || !start.canConvertToInt()
            || start.intValue() < 1)) {
          throw invalid("목록의 시작 번호가 잘못되었습니다.");
        }
        JsonNode listType = attrs.get("type");
        if (listType != null && !listType.isNull()
            && (!listType.isString() || !listType.stringValue().equals("1"))) {
          throw invalid("숫자 목록만 사용할 수 있습니다.");
        }
        if (start != null) normalized.putObject("attrs").put("start", start.intValue());
      }
    } else if (type.equals("codeBlock")) {
      if (attrs != null && !attrs.isNull()) {
        requireKeys(attrs, Set.of("language"));
        ObjectNode target = normalized.putObject("attrs");
        copyOptionalString(attrs, target, "language", 50);
      }
    } else if (attrs != null && !attrs.isNull() && !attrs.isEmpty()) {
      throw invalid("지원하지 않는 본문 속성입니다.");
    }
  }

  private void normalizeMarks(
      JsonNode node,
      String type,
      String parent,
      ObjectNode normalized
  ) {
    JsonNode marks = node.get("marks");
    if (marks == null) return;
    if (!marks.isArray() || (!type.equals("text") && !type.equals("hardBreak"))
        || "codeBlock".equals(parent)) {
      throw invalid("텍스트 서식이 올바르지 않습니다.");
    }
    if (marks.isEmpty()) return;
    Set<String> used = new HashSet<>();
    ArrayNode target = normalized.putArray("marks");
    for (JsonNode mark : marks) {
      if (!mark.isObject()) throw invalid("텍스트 서식이 올바르지 않습니다.");
      requireKeys(mark, Set.of("type"));
      String markType = requiredString(mark, "type");
      if (!MARKS.contains(markType) || !used.add(markType)) {
        throw invalid("지원하지 않거나 중복된 텍스트 서식입니다.");
      }
      target.addObject().put("type", markType);
    }
    if (used.contains("code") && used.size() > 1) {
      throw invalid("인라인 코드는 다른 텍스트 서식과 함께 사용할 수 없습니다.");
    }
  }

  private void replaceImageReferences(JsonNode node, Map<String, Long> imageIds) {
    if ("image".equals(node.path("type").asString(""))) {
      ObjectNode attrs = (ObjectNode) node.get("attrs");
      Long id = imageIds.get(attrs.get("clientId").stringValue());
      if (id == null || id < 1) throw new IllegalStateException("저장된 이미지 ID가 없습니다.");
      attrs.remove("clientId");
      attrs.put("imageId", id);
    }
    JsonNode children = node.get("content");
    if (children != null) {
      for (JsonNode child : children) replaceImageReferences(child, imageIds);
    }
  }

  private void addImageUrls(JsonNode node, Long postId, Set<Long> imageIds) {
    if ("image".equals(node.path("type").asString(""))) {
      JsonNode rawAttrs = node.get("attrs");
      if (!(rawAttrs instanceof ObjectNode attrs)) {
        throw new IllegalStateException("저장된 이미지 속성이 올바르지 않습니다.");
      }
      JsonNode rawId = attrs.get("imageId");
      if (rawId == null || !rawId.isIntegralNumber() || !rawId.canConvertToLong()
          || !imageIds.contains(rawId.longValue())) {
        throw new IllegalStateException("본문 이미지가 게시글에 연결되어 있지 않습니다.");
      }
      attrs.remove("clientId");
      attrs.put("src", PostImageResponse.imageUrl(postId, rawId.longValue()));
    }
    JsonNode children = node.get("content");
    if (children != null && children.isArray()) {
      for (JsonNode child : children) addImageUrls(child, postId, imageIds);
    }
  }

  private void requireKeys(JsonNode node, Set<String> allowed) {
    for (String key : node.propertyNames()) {
      if (!allowed.contains(key)) throw invalid("지원하지 않는 본문 속성입니다: " + key);
    }
  }

  private String requiredString(JsonNode node, String key) {
    JsonNode value = node.get(key);
    if (value == null || !value.isString()) throw invalid(key + "는 문자열이어야 합니다.");
    return value.stringValue();
  }

  private void copyOptionalString(JsonNode source, ObjectNode target, String key, int maxLength) {
    JsonNode value = source.get(key);
    if (value == null || value.isNull()) return;
    if (!value.isString() || value.stringValue().length() > maxLength) {
      throw invalid(key + " 속성의 형식이나 길이가 올바르지 않습니다.");
    }
    target.put(key, value.stringValue());
  }

  private void appendText(ValidationState state, String value) {
    state.plainText.append(value);
  }

  private static ResponseStatusException invalid(String message) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
  }

  private static class ValidationState {
    private int nodeCount;
    private final StringBuilder plainText = new StringBuilder();
    private final Set<String> imageIds = new LinkedHashSet<>();
  }
}
