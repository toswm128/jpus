package com.minsu.jpus.reaction.dto;

public interface ReactionCountProjection {

  Long getPostId();

  Long getReactionCount();

  Long getMyReactionCount();
}
