package com.minsu.jpus.global;

import com.minsu.jpus.user.UserDuplicateException;
import com.minsu.jpus.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<String> handleUserNotFound(
      UserNotFoundException e
  ){
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
  }

  @ExceptionHandler(UserDuplicateException.class)
  public ResponseEntity<String> handleUserDuplicate(
      UserDuplicateException e
  ){
    return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<String> handleBadCredentials(
      BadCredentialsException e
  ) {
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body("아이디 또는 비밀번호가 올바르지 않습니다.");
  }

}
