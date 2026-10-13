package com.minsu.jpus.auth;

import com.minsu.jpus.auth.dto.LoginRequest;
import com.minsu.jpus.user.UserService;
import com.minsu.jpus.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final SecurityContextRepository securityContextRepository;

  private final UserService userService;

  public AuthController(AuthenticationManager authenticationManager,
      SecurityContextRepository securityContextRepository, UserService userService) {
    this.authenticationManager = authenticationManager;
    this.securityContextRepository = securityContextRepository;
    this.userService = userService;
  }

  @PostMapping("/login")
  public ResponseEntity<Void> login(@RequestBody LoginRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse
  ){
    Authentication authentication =
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(
                request.username(),
                request.password()
            )
        );
    SecurityContext context =
        SecurityContextHolder.createEmptyContext();

    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    securityContextRepository.saveContext(
        context,
        httpRequest,
        httpResponse
    );

    return ResponseEntity.ok().build();
  }


  @GetMapping("/me")
  public UserResponse me(Authentication authentication) {
    return userService.getUserByUsername(authentication.getName());
  }
}
