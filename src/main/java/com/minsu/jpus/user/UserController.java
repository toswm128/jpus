package com.minsu.jpus.user;

import com.minsu.jpus.user.dto.CreateUserRequest;
import com.minsu.jpus.user.dto.UserResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService){
    this.userService = userService;
  }

  @GetMapping
  public List<UserResponse> getUsers(){
    return userService.getUsers();
  }

  @GetMapping("/{id}")
  public UserResponse getUser(@PathVariable Long id){
    return userService.getUser(id);
  }

  @PostMapping
  public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request){
    UserResponse user = userService.createUser(request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(user);
  }


}
