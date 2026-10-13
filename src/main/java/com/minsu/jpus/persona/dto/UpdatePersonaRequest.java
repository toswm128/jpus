package com.minsu.jpus.persona.dto;

public record UpdatePersonaRequest(
    String displayName,
    String bio
) {}