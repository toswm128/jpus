package com.minsu.jpus.comment;

import com.minsu.jpus.auth.UserPrincipal;
import com.minsu.jpus.comment.dto.CommentResponse;
import com.minsu.jpus.comment.dto.CreateCommentRequest;
import com.minsu.jpus.comment.dto.UpdateCommentRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts/{postId}/comments")
public class CommentController {
  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @PostMapping
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

  @GetMapping
  public List<CommentResponse> getComments(
      @PathVariable("postId") Long postId
  ){
    return commentService.getComments(postId);
  }

  @PatchMapping("/{commentId}")
  public ResponseEntity<CommentResponse> patchComment(
      @PathVariable("postId") Long postId,
      @PathVariable("commentId") Long commentId,
      @RequestBody UpdateCommentRequest request,
      Authentication authentication
  ){
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(commentService.patchComment(commentId,postId, userPrincipal.getId(),request));
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<CommentResponse> deleteComment(
      @PathVariable("postId") Long postId,
      @PathVariable("commentId") Long commentId,
      Authentication authentication
  ){
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    commentService.deleteComment(commentId,postId, userPrincipal.getId());

    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }


}

