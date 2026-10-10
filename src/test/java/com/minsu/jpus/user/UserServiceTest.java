package com.minsu.jpus.user;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.minsu.jpus.user.dto.CreateUserRequest;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository);
  }

  @Test
  void createUserTest(){
    when(userRepository.existsByUsername("minsu"))
        .thenReturn(false);

    when(userRepository.save(any(User.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    User user = userService.createUser(new CreateUserRequest("minsu","조조민수"));

    verify(userRepository).save(any(User.class));
    assertEquals("minsu",user.getUsername());
    assertEquals("조조민수",user.getNickname());
  }

  @Test
  void createUseruDplicateTest(){
    when(userRepository.existsByUsername("minsu"))
        .thenReturn(true);

    verify(userRepository, never()).save(any(User.class));
    assertThrows(UserDuplicateException.class,()->userService.createUser(new CreateUserRequest("minsu","조민수")));
  }

  @Test
  void getUsersTest(){
    when(userRepository.findAll())
        .thenAnswer(invocation -> new ArrayList<>());
    List<User> users = userService.getUsers();
    verify(userRepository).findAll();
  }

  @Test
  void getUserTest(){
    when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(new User("minsu", "조조민수")));
    userService.getUser(1L);

    verify(userRepository).findById(any(Long.class));
  }

  @Test
  void getUserNotFoundTest(){
    when(userRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(null));
    assertThrows(UserNotFoundException.class,()->userService.getUser(3L));
  }
}
