package com.minsu.jpus.user;

public class UserNotFoundException extends RuntimeException{
  public UserNotFoundException(String key,String value) {
    super(String.format("존재하지 않는 유저입니다. %s= %s",key,value));
  }

}