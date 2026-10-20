package com.minsu.jpus.comment;

import com.minsu.jpus.auth.UserPrincipal;
import com.minsu.jpus.comment.dto.CommentResponse;
import com.minsu.jpus.comment.dto.CreateCommentRequest;
import com.minsu.jpus.post.dto.CreatePostRequest;
import com.minsu.jpus.post.dto.PostResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment")
public class CommentController {
  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @PostMapping("/{postId}")
  public ResponseEntity<CommentResponse> createComment(
      @RequestBody CreateCommentRequest request,
      Authentication authentication,
      @PathVariable("postId") Long postId
  ){
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(commentService.createComment(postId, userPrincipal.getId(), request));
  }
}

