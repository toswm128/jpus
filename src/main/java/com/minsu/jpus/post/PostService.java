package com.minsu.jpus.post;


import com.minsu.jpus.comment.CommentRepository;
import com.minsu.jpus.comment.dto.CommentCountProjection;
import com.minsu.jpus.persona.Persona;
import com.minsu.jpus.persona.PersonaNotFoundException;
import com.minsu.jpus.persona.PersonaRepository;
import com.minsu.jpus.post.dto.CreatePostRequest;
import com.minsu.jpus.post.dto.CreateRichPostRequest;
import com.minsu.jpus.post.dto.PostDetailResponse;
import com.minsu.jpus.post.dto.PostImageResponse;
import com.minsu.jpus.post.dto.PostResponse;
import com.minsu.jpus.post.dto.PostSummaryResponse;
import com.minsu.jpus.post.dto.UpdatePostRequest;
import com.minsu.jpus.reaction.ReactionRepository;
import com.minsu.jpus.reaction.dto.ReactionCountProjection;
import com.minsu.jpus.storage.LocalImageStorage;
import com.minsu.jpus.user.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostService {

  private static final Logger logger = LoggerFactory.getLogger(PostService.class);
  private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

  private final PostRepository postRepository;
  private final PersonaRepository personaRepository;
  private final CommentRepository commentRepository;
  private final ReactionRepository reactionRepository;
  private final PostImageRepository postImageRepository;
  private final LocalImageStorage imageStorage;
  private final RichPostContent richPostContent;
  private final TransactionTemplate createTransaction;

  public PostService(
      PostRepository postRepository,
      PersonaRepository personaRepository,
      CommentRepository commentRepository,
      ReactionRepository reactionRepository,
      PostImageRepository postImageRepository,
      LocalImageStorage imageStorage,
      RichPostContent richPostContent,
      PlatformTransactionManager transactionManager
  ) {
    this.postRepository = postRepository;
    this.personaRepository = personaRepository;
    this.commentRepository = commentRepository;
    this.reactionRepository = reactionRepository;
    this.postImageRepository = postImageRepository;
    this.imageStorage = imageStorage;
    this.richPostContent = richPostContent;
    this.createTransaction = new TransactionTemplate(transactionManager);
    this.createTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  private Map<Long, PostSummaryResponse> buildResponses(
      List<Post> posts,
      Long userId
  ) {
    if (posts.isEmpty()) {
      return Map.of();
    }

    List<Long> postIds = posts.stream()
        .map(Post::getId)
        .toList();

    Map<Long, Long> commentCounts =
        commentRepository.countCommentsByPostIds(postIds).stream()
            .collect(Collectors.toMap(
                CommentCountProjection::getPostId,
                CommentCountProjection::getCommentCount
            ));

    Map<Long, ReactionCountProjection> reactionCounts =
        reactionRepository.countReactionsByPostIds(postIds, userId).stream()
            .collect(Collectors.toMap(
                ReactionCountProjection::getPostId,
                row -> row
            ));

    Map<Long, PostSummaryResponse> responses = new HashMap<>();

    for (Post post : posts) {
      ReactionCountProjection reaction = reactionCounts.get(post.getId());

      responses.put(post.getId(), PostSummaryResponse.from(
          post,
          commentCounts.getOrDefault(post.getId(), 0L),
          reaction == null ? 0L : reaction.getReactionCount(),
          reaction != null && reaction.getMyReactionCount() > 0
      ));
    }

    return responses;
  }

  public PostResponse createPost(Long userId, CreatePostRequest request) {
    validateTitle(request.title());
    validatePlainContent(request.content());
    Persona persona = ownedPersona(request.personaId(), userId);

    User user = persona.getUser();
    return PostResponse.from(
        postRepository.save(new Post(user, persona, request.title(), request.content())));
  }

  public PostResponse createRichPost(
      Long userId,
      CreateRichPostRequest request,
      List<MultipartFile> images
  ) {
    validateTitle(request.title());
    RichPostContent.ValidatedContent content =
        richPostContent.validate(request.contentJson(), request.imageClientIds());
    List<MultipartFile> files = images == null ? List.of() : images;
    validateFiles(files, content.imageClientIds().size());
    ownedPersona(request.personaId(), userId);

    List<StoredUpload> uploaded = new ArrayList<>();
    try {
      for (int i = 0; i < files.size(); i++) {
        var stored = imageStorage.save(files.get(i));
        uploaded.add(new StoredUpload(
            content.imageClientIds().get(i), stored.storageKey(), stored.contentType()
        ));
      }

      // execute returns after commit, so this catch also covers commit failures.
      return createTransaction.execute(status -> {
        Persona persona = ownedPersona(request.personaId(), userId);
        Post post = postRepository.save(new Post(
            persona.getUser(), persona, request.title(), content.plainText()
        ));
        Map<String, Long> imageIds = new HashMap<>();
        for (int i = 0; i < uploaded.size(); i++) {
          StoredUpload upload = uploaded.get(i);
          PostImage image = postImageRepository.save(new PostImage(
              post, upload.storageKey(), upload.contentType(), i
          ));
          imageIds.put(upload.clientId(), image.getId());
        }
        post.initializeRichContent(
            content.plainText(), richPostContent.attachImages(content, imageIds)
        );
        return PostResponse.from(post);
      });
    } catch (Exception failure) {
      RuntimeException problem = failure instanceof RuntimeException runtime
          ? runtime : new IllegalStateException("이미지를 저장하지 못했습니다.", failure);
      for (StoredUpload upload : uploaded) {
        try {
          imageStorage.delete(upload.storageKey());
        } catch (Exception cleanupFailure) {
          problem.addSuppressed(cleanupFailure);
          logger.warn("Failed to clean up uncommitted post image {}", upload.storageKey(),
              cleanupFailure);
        }
      }
      throw problem;
    }
  }

  @Transactional(readOnly = true)
  public Page<PostSummaryResponse> getPosts(int page, int size, Long userId) {
    Page<Post> posts =
        postRepository.findAllByDeletedAtIsNullOrderByCreatedAtDescIdDesc(
            PageRequest.of(page, size)
        );

    Map<Long, PostSummaryResponse> responses =
        buildResponses(posts.getContent(), userId);

    return posts.map(post -> responses.get(post.getId()));
  }

  @Transactional(readOnly = true)
  public PostDetailResponse getPost(Long postId, Long userId) {
    Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
        .orElseThrow(() -> new PostNotFoundException("post id", postId));

    PostSummaryResponse summary = buildResponses(List.of(post), userId).get(postId);
    List<PostImageResponse> images = postImageRepository
        .findAllByPost_IdOrderBySortOrderAscIdAsc(postId).stream()
        .map(image -> PostImageResponse.from(image, postId))
        .toList();
    Set<Long> imageIds = images.stream()
        .map(PostImageResponse::id)
        .collect(Collectors.toSet());
    return PostDetailResponse.from(
        summary, richPostContent.forResponse(post.getContentJson(), postId, imageIds), images
    );
  }

  public record ImageContent(Resource resource, String contentType) {
  }

  @Transactional(readOnly = true)
  public ImageContent getImage(Long postId, Long imageId) {
    PostImage image = postImageRepository
        .findByIdAndPost_IdAndPost_DeletedAtIsNull(imageId, postId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이미지가 없습니다."));
    try {
      Resource resource = imageStorage.load(image.getStorageKey());
      if (!resource.exists() || !resource.isReadable()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "이미지가 없습니다.");
      }
      return new ImageContent(resource, image.getContentType());
    } catch (Exception failure) {
      if (failure instanceof RuntimeException runtime) throw runtime;
      throw new IllegalStateException("이미지를 읽지 못했습니다.", failure);
    }
  }

  @Transactional
  public void deletePost(Long postId, Long userId) {
    Post post = postRepository.findByIdAndUser_IdAndDeletedAtIsNull(postId, userId)
        .orElseThrow(() -> new PostNotFoundException("post id", postId.toString()));

    post.delete();
  }

  @Transactional
  public PostResponse patchPost(
      Long postId,
      Long userId,
      UpdatePostRequest request
  ) {
    Post post =
        postRepository.findByIdAndUser_IdAndDeletedAtIsNull(postId, userId).orElseThrow(
            () -> new PostNotFoundException("post id", postId.toString())
        );

    if (post.getContentJson() != null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "서식이 있는 게시글은 일반 텍스트 수정 API로 변경할 수 없습니다."
      );
    }
    validateTitle(request.title());
    validatePlainContent(request.content());
    post.updateContent(request.title(), request.content());
    return PostResponse.from(post);
  }

  private Persona ownedPersona(Long personaId, Long userId) {
    if (personaId == null || personaId < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "페르소나를 선택해 주세요.");
    }
    return personaRepository.findByIdAndUser_IdAndDeletedAtIsNull(personaId, userId)
        .orElseThrow(() -> new PersonaNotFoundException("persona id", personaId.toString()));
  }

  private void validateTitle(String title) {
    if (title == null || title.isBlank() || title.length() > 100) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "제목은 공백을 제외한 내용이 있어야 하며 100자 이하여야 합니다."
      );
    }
  }

  private void validatePlainContent(String content) {
    if (content == null || content.isBlank() || content.length() > RichPostContent.MAX_TEXT_LENGTH) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "본문을 입력해 주세요. 본문은 20,000자 이하여야 합니다."
      );
    }
  }

  private void validateFiles(List<MultipartFile> images, int expectedCount) {
    if (images.size() != expectedCount || images.size() > RichPostContent.MAX_IMAGES) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "첨부 파일 목록과 본문 이미지 참조가 일치하지 않습니다."
      );
    }
    for (MultipartFile image : images) {
      if (image == null || image.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "빈 이미지는 첨부할 수 없습니다.");
      }
      if (image.getSize() > MAX_IMAGE_BYTES) {
        throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "이미지는 파일당 5MiB 이하여야 합니다.");
      }
    }
  }

  private record StoredUpload(String clientId, String storageKey, String contentType) {
  }
}
