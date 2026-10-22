package com.minsu.jpus.comment.dto;

import com.minsu.jpus.comment.Comment;
import com.minsu.jpus.persona.Persona;
import com.minsu.jpus.post.Post;
import com.minsu.jpus.user.User;
import java.time.LocalDateTime;

public record CommentResponse(
    Long id,
    Long postId,
    Long personaId,
    String content,
    String authorNameSnapshot,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {
    public static CommentResponse from(Comment comment){
      return new CommentResponse(
          comment.getId(),
          comment.getPost().getId(),
          comment.getPersona().getId(),
          comment.getContent(),
          comment.getAuthorNameSnapshot(),
          comment.getCreatedAt(),
          comment.getUpdatedAt()
      );
    }
}
