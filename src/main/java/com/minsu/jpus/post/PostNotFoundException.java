package com.minsu.jpus.post;

public class PostNotFoundException extends RuntimeException{
  public PostNotFoundException(String key,Object value) {
    super(String.format("존재하지 않는 게시글입니다. %s= %s",key,value));
  }

}