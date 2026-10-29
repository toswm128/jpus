package com.minsu.jpus.post.dto;

import com.minsu.jpus.post.Post;
import java.time.LocalDateTime;

public record PostResponse(
    Long id,
    String title,
    String content,
    Long personaId,
    String authorNameSnapshot,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {


  public static PostResponse from(Post post) {
    return new PostResponse(
        post.getId(),
        post.getTitle(),
        post.getContent(),
        post.getPersona().getId(),
        post.getAuthorNameSnapshot(),
        post.getCreatedAt(),
        post.getUpdatedAt()
    );
  }
}
