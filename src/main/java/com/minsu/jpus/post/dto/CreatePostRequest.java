package com.minsu.jpus.post.dto;

public record CreatePostRequest(
    Long personaId,
    String title,
    String content
) {

}
