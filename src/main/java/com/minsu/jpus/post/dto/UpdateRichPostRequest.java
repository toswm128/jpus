package com.minsu.jpus.post.dto;

import java.util.List;
import tools.jackson.databind.JsonNode;

public record UpdateRichPostRequest(
    String title,
    JsonNode contentJson,
    List<String> imageClientIds
) {

}