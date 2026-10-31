package com.minsu.jpus.post.dto;

import java.time.LocalDateTime;
import java.util.List;
import tools.jackson.databind.JsonNode;

public record PostDetailResponse(
    Long id,
    String title,
    String content,
    Long personaId,
    String authorNameSnapshot,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long commentCount,
    Long reactionCount,
    Boolean reactedByMe,
    JsonNode contentJson,
    List<PostImageResponse> images,
    boolean isMine
) {

  public static PostDetailResponse from(
      PostSummaryResponse summary,
      JsonNode contentJson,
      List<PostImageResponse> images,
      boolean isMine
  ) {
    return new PostDetailResponse(
        summary.id(),
        summary.title(),
        summary.content(),
        summary.personaId(),
        summary.authorNameSnapshot(),
        summary.createdAt(),
        summary.updatedAt(),
        summary.commentCount(),
        summary.reactionCount(),
        summary.reactedByMe(),
        contentJson,
        List.copyOf(images),
        isMine
    );
  }
}
