package com.minsu.jpus.comment.dto;

public record CreateCommentRequest(
    Long personaId,
    String content
) {
}
