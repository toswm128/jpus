package com.minsu.jpus.user;

public class UserNotFoundException extends RuntimeException{
  public UserNotFoundException(String message) {
    super(message);
  }

}