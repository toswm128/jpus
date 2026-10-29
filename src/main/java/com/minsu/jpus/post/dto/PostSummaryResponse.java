package com.minsu.jpus.post.dto;

import com.minsu.jpus.post.Post;
import java.time.LocalDateTime;


public record PostSummaryResponse(
    Long id,
    String title,
    String content,
    Long personaId,
    String authorNameSnapshot,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long commentCount,
    Long reactionCount,
    Boolean reactedByMe
) {


  public static PostSummaryResponse from(
      Post post,
      long commentCount,
      long reactionCount,
      boolean reactedByMe
  ) {
    return new PostSummaryResponse(
        post.getId(),
        post.getTitle(),
        post.getContent(),
        post.getPersona().getId(),
        post.getAuthorNameSnapshot(),
        post.getCreatedAt(),
        post.getUpdatedAt(),
        commentCount,
        reactionCount,
        reactedByMe
    );
  }
}
