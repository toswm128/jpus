package com.minsu.jpus.user.dto;

public record CreateUserRequest(
    String username,
    String password,
    String nickname
) {
}