package com.minsu.jpus.auth;

import com.minsu.jpus.user.UserService;
import com.minsu.jpus.user.dto.UserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

  private final UserService userService;

  public AuthController(UserService userService) {
    this.userService = userService;
  }


  @GetMapping("/me")
  public UserResponse me(Authentication authentication) {
    return userService.getUserByUsername(authentication.getName());
  }
}
