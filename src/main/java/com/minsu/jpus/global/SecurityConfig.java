package com.minsu.jpus.global;

import com.minsu.jpus.user.User;
import com.minsu.jpus.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.POST, "/users").permitAll()
        .anyRequest().authenticated()
    );

    http.csrf(csrf -> csrf
        .ignoringRequestMatchers(
            "/users",
            "/auth/login",
            "/auth/logout"
        )
    );

    http.exceptionHandling(exception -> exception
        .authenticationEntryPoint((request, response, e) ->
            response.sendError(401)
        )
    );

    http.logout(logout -> logout
        .logoutUrl("/auth/logout")
        .logoutSuccessHandler((request, response, authentication) ->
            response.setStatus(200)
        )
    );

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService(UserRepository userRepository) {
    return username -> {
      User user = userRepository.findByUsername(username)
          .orElseThrow(() ->
              new UsernameNotFoundException("존재하지 않는 사용자입니다.")
          );

      return org.springframework.security.core.userdetails.User
          .withUsername(user.getUsername())
          .password(user.getPasswordHash())
          .authorities("USER")
          .build();
    };
  }
}