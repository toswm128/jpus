package com.minsu.jpus.basic;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/greeting")
public class GreetingController {
  private GreetingService greetingService;

  public GreetingController(GreetingService greetingService){
    this.greetingService = greetingService;
  }

  @GetMapping("/hello")
  public String hello(@RequestParam(defaultValue = "world") String name){
    return greetingService.greet(name);
  }

  @GetMapping("hello/{age}")
  public String helloAge(@RequestParam(defaultValue = "world") String name,@PathVariable("age") int age){
    return greetingService.helloAge(name,age);
  }

  @PostMapping("/users")
  public ResponseEntity<CreateUserRequest> createUser(
      @RequestBody CreateUserRequest request
  ) {
    return ResponseEntity.
        status(HttpStatus.CREATED)
        .body(request);
  }
}
