package com.minsu.jpus.post;

import com.minsu.jpus.auth.UserPrincipal;
import com.minsu.jpus.post.dto.CreatePostRequest;
import com.minsu.jpus.post.dto.CreateRichPostRequest;
import com.minsu.jpus.post.dto.PostDetailResponse;
import com.minsu.jpus.post.dto.PostResponse;
import com.minsu.jpus.post.dto.PostSummaryResponse;
import com.minsu.jpus.post.dto.UpdatePostRequest;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
public class PostController {

  private final PostService postService;

  public PostController(PostService postService) {
    this.postService = postService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PostResponse> createPost(
      @RequestBody CreatePostRequest request,
      Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    PostResponse post = postService.createPost(userPrincipal.getId(), request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(post);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<PostResponse> createRichPost(
      @RequestPart("post") CreateRichPostRequest request,
      @RequestPart(value = "images", required = false) List<MultipartFile> images,
      Authentication authentication
  ) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(postService.createRichPost(userPrincipal.getId(), request, images));
  }

  @GetMapping
  public PagedModel<PostSummaryResponse> getPosts(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "20") int size,
      Authentication authentication
  ) {
    if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "page는 0 이상, size는 1~100이며 페이지 위치는 지원 범위 이내여야 합니다."
      );
    }
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    return new PagedModel<>(postService.getPosts(page, size, userPrincipal.getId()));

  }

  @GetMapping("/{id}")
  public PostDetailResponse getPost(
      @PathVariable("id") Long postId,
      Authentication authentication
  ) {

    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    return postService.getPost(postId, userPrincipal.getId());
  }

  @GetMapping("/{postId}/images/{imageId}")
  public ResponseEntity<Resource> getImage(
      @PathVariable("postId") Long postId,
      @PathVariable("imageId") Long imageId
  ) {
    PostService.ImageContent image = postService.getImage(postId, imageId);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(image.contentType()))
        .cacheControl(CacheControl.noStore())
        .header("X-Content-Type-Options", "nosniff")
        .body(image.resource());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePost(
      @PathVariable("id") Long postId,
      Authentication authentication
  ) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    postService.deletePost(postId, userPrincipal.getId());

    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();

  }

  @PatchMapping("/{id}")
  public ResponseEntity<PostResponse> patchPost(
      @PathVariable("id") Long postId,
      @RequestBody UpdatePostRequest request,
      Authentication authentication
  ) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    PostResponse post = postService.patchPost(postId, userPrincipal.getId(), request);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(post);

  }
}
