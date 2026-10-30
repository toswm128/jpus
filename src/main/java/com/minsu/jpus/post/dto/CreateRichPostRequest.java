package com.minsu.jpus.post.dto;

import java.util.List;
import tools.jackson.databind.JsonNode;

public record CreateRichPostRequest(
    Long personaId,
    String title,
    JsonNode contentJson,
    List<String> imageClientIds
) {
}
