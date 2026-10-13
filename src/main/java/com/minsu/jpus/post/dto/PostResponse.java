package com.minsu.jpus.post.dto;

import com.minsu.jpus.post.Post;

public record PostResponse(
    Long id,

    String content
) {

  public static PostResponse from(Post post){
    return new PostResponse(
        post.getId(),
        post.getContent()
    );
  }
}
