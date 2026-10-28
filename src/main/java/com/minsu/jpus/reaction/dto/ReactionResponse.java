package com.minsu.jpus.reaction.dto;

import com.minsu.jpus.reaction.Reaction;
import java.util.List;

public record ReactionResponse(
    int reactionCount,
    boolean reactedByMe
) {

  public static ReactionResponse from(List<Reaction> reactions, Long userId) {
    int reactionCount = reactions.size();
    boolean reactedByMe = reactions
        .stream()
        .anyMatch(reaction -> reaction.getUser().getId().equals(userId));

    return new ReactionResponse(reactionCount, reactedByMe);
  }
}
