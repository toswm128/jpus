package com.minsu.jpus.comment.dto;

public interface CommentCountProjection {

  Long getPostId();

  Long getCommentCount();
}