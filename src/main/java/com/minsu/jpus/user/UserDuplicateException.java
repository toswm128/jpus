package com.minsu.jpus.user;

public class UserDuplicateException extends RuntimeException{
  public UserDuplicateException(String message) {
    super(message);
  }

}