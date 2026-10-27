package com.minsu.jpus.comment.dto;

import com.minsu.jpus.comment.Comment;
import java.time.LocalDateTime;

public record CommentResponse(
    Long id,
    Long postId,
    Long personaId,
    String content,
    String authorNameSnapshot,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean isMine
) {

  public static CommentResponse from(Comment comment, Long currentUserId) {
    return new CommentResponse(
        comment.getId(),
        comment.getPost().getId(),
        comment.getPersona().getId(),
        comment.getContent(),
        comment.getAuthorNameSnapshot(),
        comment.getCreatedAt(),
        comment.getUpdatedAt(),
        comment.getUser().getId().equals(currentUserId)
    );
  }
}
