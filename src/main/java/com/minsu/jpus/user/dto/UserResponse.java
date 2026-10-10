package com.minsu.jpus.user.dto;

import com.minsu.jpus.user.User;

public record UserResponse(
    Long id,
    String username,
    String nickname
) {
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getNickname()
    );
  }
}