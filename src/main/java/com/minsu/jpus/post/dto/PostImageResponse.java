package com.minsu.jpus.post.dto;

import com.minsu.jpus.post.PostImage;

public record PostImageResponse(
    Long id,
    String url,
    String contentType,
    int sortOrder
) {

  public static PostImageResponse from(PostImage image, Long postId) {
    return new PostImageResponse(
        image.getId(),
        imageUrl(postId, image.getId()),
        image.getContentType(),
        image.getSortOrder()
    );
  }

  public static String imageUrl(Long postId, Long imageId) {
    return "/posts/" + postId + "/images/" + imageId;
  }
}
