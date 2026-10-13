package com.minsu.jpus.auth.dto;

public record LoginRequest(
    String username,
    String password
) {

}
