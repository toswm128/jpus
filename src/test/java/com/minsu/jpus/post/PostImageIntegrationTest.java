package com.minsu.jpus.post;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:jpus_inline_images;MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.open-in-view=false",
    "spring.main.banner-mode=off",
    "logging.level.root=WARN"
})
@AutoConfigureMockMvc
class PostImageIntegrationTest {

  private static final Path UPLOADS = temporaryDirectory();

  @Autowired MockMvc mvc;
  @Autowired JsonMapper mapper;
  @Autowired PostRepository posts;
  @Autowired PostImageRepository images;

  @DynamicPropertySource
  static void uploadDirectory(DynamicPropertyRegistry registry) {
    registry.add("app.upload-dir", UPLOADS::toString);
  }

  @AfterAll
  static void removeTemporaryImages() throws IOException {
    try (var paths = Files.walk(UPLOADS)) {
      for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
        Files.deleteIfExists(path);
      }
    }
  }

  @Test
  void inlineImagesRoundTripAndDeletedPostsHideTheirImages() throws Exception {
    Account account = account();
    JsonNode created = rich(account, """
        {"type":"doc","content":[
          {"type":"paragraph","content":[{"type":"text","text":"사진 앞","marks":[{"type":"bold"}]}]},
          {"type":"image","attrs":{"clientId":"first","alt":"첫 사진"}},
          {"type":"paragraph","content":[{"type":"text","text":"사진 사이"}]},
          {"type":"image","attrs":{"clientId":"second"}},
          {"type":"paragraph","content":[{"type":"text","text":"사진 뒤"}]}
        ]}
        """, List.of("first", "second"), png(), png());
    long postId = created.get("id").longValue();
    JsonNode detail = body(mvc.perform(get("/posts/" + postId).session(account.session()))
        .andExpect(status().isOk()).andReturn());
    assertEquals(List.of("사진 앞", "사진 사이", "사진 뒤"),
        detail.get("content").stringValue().lines().filter(line -> !line.isBlank()).toList());
    assertEquals(2, detail.get("images").size());
    JsonNode nodes = detail.get("contentJson").get("content");
    long imageId = nodes.get(1).get("attrs").get("imageId").longValue();
    assertEquals("사진 뒤", nodes.get(4).get("content").get(0).get("text").stringValue());
    String stored = posts.findById(postId).orElseThrow().getContentJson();
    assertFalse(stored.contains("clientId"));
    assertFalse(stored.contains("blob:"));
    assertFalse(stored.contains("\"src\""));
    String url = "/posts/" + postId + "/images/" + imageId;
    mvc.perform(get(url)).andExpect(status().isUnauthorized());
    MvcResult image = mvc.perform(get(url).session(account.session()))
        .andExpect(status().isOk()).andReturn();
    assertEquals("image/png", image.getResponse().getContentType());
    assertArrayEquals(png(), image.getResponse().getContentAsByteArray());
    mvc.perform(get("/posts/999999/images/" + imageId).session(account.session()))
        .andExpect(status().isNotFound());
    mvc.perform(delete("/posts/" + postId).session(account.session()))
        .andExpect(status().isNoContent());
    mvc.perform(get(url).session(account.session())).andExpect(status().isNotFound());
    mvc.perform(get("/posts/" + postId).session(account.session()))
        .andExpect(status().isNotFound());
  }

  @Test
  void invalidSecondImageCleansFirstFileWithoutSavingThePost() throws Exception {
    Account account = account();
    long fileCount = fileCount();
    long postCount = posts.count();
    long imageCount = images.count();
    mvc.perform(multipart("/posts")
        .file(postPart(account.personaId(), """
            {"type":"doc","content":[
              {"type":"image","attrs":{"clientId":"first"}},
              {"type":"image","attrs":{"clientId":"second"}}
            ]}
            """, List.of("first", "second")))
        .file(new MockMultipartFile("images", "one.png", "image/png", png()))
        .file(new MockMultipartFile("images", "fake.png", "image/png", "not an image".getBytes()))
        .session(account.session()))
        .andExpect(status().isBadRequest());
    assertEquals(fileCount, fileCount());
    assertEquals(postCount, posts.count());
    assertEquals(imageCount, images.count());
  }

  @Test
  void foreignPersonaAndForgedImageReferencesCannotStoreFiles() throws Exception {
    Account owner = account();
    Account other = account();
    long count = fileCount();
    String document = "{\"type\":\"doc\",\"content\":[{\"type\":\"image\",\"attrs\":{\"clientId\":\"photo\"}}]}";
    mvc.perform(multipart("/posts")
        .file(postPart(owner.personaId(), document, List.of("photo")))
        .file(new MockMultipartFile("images", "photo.png", "image/png", png()))
        .session(other.session())).andExpect(status().isNotFound());
    mvc.perform(multipart("/posts")
        .file(postPart(owner.personaId(), "{\"type\":\"doc\",\"content\":[{\"type\":\"image\",\"attrs\":{\"imageId\":1}}]}", List.of()))
        .session(owner.session())).andExpect(status().isBadRequest());
    mvc.perform(multipart("/posts")
        .file(postPart(owner.personaId(), document, List.of("different")))
        .file(new MockMultipartFile("images", "photo.png", "image/png", png()))
        .session(owner.session())).andExpect(status().isBadRequest());
    assertEquals(count, fileCount());
  }

  @Test
  void imageOnlyAndFormattedTextPostsWorkAndPlainPostsRemainCompatible() throws Exception {
    Account account = account();
    JsonNode photo = rich(account,
        "{\"type\":\"doc\",\"content\":[{\"type\":\"image\",\"attrs\":{\"clientId\":\"photo\"}}]}",
        List.of("photo"), png());
    assertEquals("", photo.get("content").stringValue());
    JsonNode formatted = rich(account,
        "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"서식 있는 글\",\"marks\":[{\"type\":\"italic\"}]}]}]}",
        List.of());
    mvc.perform(patch("/posts/" + formatted.get("id").longValue()).session(account.session())
        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"제목\",\"content\":\"본문\"}"))
        .andExpect(status().isBadRequest());
    String plainText = "기존 글".repeat(200);
    JsonNode plain = body(mvc.perform(post("/posts").session(account.session())
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(java.util.Map.of(
            "personaId", account.personaId(), "title", "기존 작성 방식", "content", plainText))))
        .andExpect(status().isCreated()).andReturn());
    JsonNode detail = body(mvc.perform(get("/posts/" + plain.get("id").longValue()).session(account.session()))
        .andExpect(status().isOk()).andReturn());
    assertEquals(plainText, detail.get("content").stringValue());
    assertTrue(detail.get("contentJson").isNull());
    assertEquals(0, detail.get("images").size());
    mvc.perform(patch("/posts/" + plain.get("id").longValue()).session(account.session())
        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"수정\",\"content\":\"본문 수정\"}"))
        .andExpect(status().isOk());
  }

  private JsonNode rich(Account account, String document, List<String> ids, byte[]... files) throws Exception {
    var request = multipart("/posts").file(postPart(account.personaId(), document, ids));
    for (byte[] bytes : files) request.file(new MockMultipartFile("images", "photo.png", "image/png", bytes));
    return body(mvc.perform(request.session(account.session())).andExpect(status().isCreated()).andReturn());
  }

  private MockMultipartFile postPart(long personaId, String document, List<String> ids) {
    var json = mapper.createObjectNode();
    json.put("personaId", personaId);
    json.put("title", "이미지 테스트");
    json.set("contentJson", mapper.readTree(document));
    var refs = json.putArray("imageClientIds");
    ids.forEach(refs::add);
    return new MockMultipartFile("post", "post.json", "application/json", mapper.writeValueAsBytes(json));
  }

  private Account account() throws Exception {
    String username = "image_" + UUID.randomUUID().toString().replace("-", "");
    mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(java.util.Map.of(
            "username", username, "password", "test-password", "nickname", "테스트"))))
        .andExpect(status().isCreated());
    MvcResult login = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(java.util.Map.of("username", username, "password", "test-password"))))
        .andExpect(status().isOk()).andReturn();
    var session = (MockHttpSession) login.getRequest().getSession(false);
    JsonNode persona = body(mvc.perform(post("/persona").session(session).contentType(MediaType.APPLICATION_JSON)
        .content("{\"displayName\":\"이미지 테스트\",\"bio\":\"테스트\"}"))
        .andExpect(status().isCreated()).andReturn());
    return new Account(session, persona.get("id").longValue());
  }

  private JsonNode body(MvcResult result) {
    return mapper.readTree(result.getResponse().getContentAsByteArray());
  }

  private long fileCount() throws IOException {
    try (var paths = Files.list(UPLOADS)) {
      return paths.count();
    }
  }

  private static byte[] png() throws IOException {
    var output = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
    return output.toByteArray();
  }

  private static Path temporaryDirectory() {
    try {
      return Files.createTempDirectory("jpus-image-integration-");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private record Account(MockHttpSession session, long personaId) {
  }
}
