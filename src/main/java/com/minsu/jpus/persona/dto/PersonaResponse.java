package com.minsu.jpus.persona.dto;

import com.minsu.jpus.persona.Persona;

public record PersonaResponse(
    Long id,
    String displayName,
    String bio,
    String profileImageKey
) {
  public static PersonaResponse from(Persona persona) {
    return new PersonaResponse(
        persona.getId(),
        persona.getDisplayName(),
        persona.getBio(),
        persona.getProfileImageKey()
    );
  }
}