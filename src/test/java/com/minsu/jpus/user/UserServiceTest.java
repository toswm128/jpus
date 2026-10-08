package com.minsu.jpus.user;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.minsu.jpus.user.dto.CreateUserRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

public class UserServiceTest {

  @Test
  void createUserTest(){
    UserService userService = new UserService(new UserRepository());

    User user = userService.createUser(new CreateUserRequest("minsu","조조민수"));

    assertEquals(1L,user.getId());
    assertEquals("minsu",user.getUserName());
    assertEquals("조조민수",user.getNickname());
  }

  @Test
  void getUsersTest(){
    UserService userService = new UserService(new UserRepository());
    userService.createUser(new CreateUserRequest("minsu","조조민수"));
    userService.createUser(new CreateUserRequest("wpdnjs","제원"));

    List<User> users = userService.getUsers();

    assertEquals(2, users.size());
    assertEquals("minsu", users.getFirst().getUserName());
    assertEquals("조조민수", users.getFirst().getNickname());
    assertEquals("wpdnjs", users.get(1).getUserName());
    assertEquals("제원", users.get(1).getNickname());
  }

  @Test
  void getUserTest(){
    UserService userService = new UserService(new UserRepository());
    userService.createUser(new CreateUserRequest("minsu","조조민수"));
    userService.createUser(new CreateUserRequest("wpdnjs","제원"));

    User user = userService.getUser(1L);

    assertEquals(1L, user.getId());
    assertEquals("minsu", user.getUserName());
  }

  @Test
  void getUserNotFoundTest(){
    UserService userService = new UserService(new UserRepository());
    userService.createUser(new CreateUserRequest("minsu","조조민수"));
    userService.createUser(new CreateUserRequest("wpdnjs","제원"));

    assertThrows(UserNotFoundException.class,()->userService.getUser(3L));
  }
}
