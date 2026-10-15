package com.minsu.jpus.post;

import com.minsu.jpus.auth.UserPrincipal;
import com.minsu.jpus.post.dto.CreatePostRequest;
import com.minsu.jpus.post.dto.PostResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class PostController {
  private final PostService postService;

  public PostController(PostService postService) {
    this.postService = postService;
  }

  @PostMapping
  public ResponseEntity<PostResponse> createtPost(
      @RequestBody CreatePostRequest request,
      Authentication authentication)
  {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    PostResponse post = postService.createPost(userPrincipal.getId(),request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(post);
  }

  @GetMapping
  public List<PostResponse> getPosts(){

    return postService.getPosts();

  }

  @GetMapping("/{id}")
  public PostResponse getPost(@PathVariable("id") Long postId){

    return postService.getPost(postId);

  }
}
