package com.minsu.jpus.comment;

public class CommentNotFoundException extends RuntimeException{
  public CommentNotFoundException(String key,Object value) {
    super(String.format("존재하지 않는 댓글입니다. %s= %s",key,value));
  }

}