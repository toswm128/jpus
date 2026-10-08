package com.minsu.jpus.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.minsu.jpus.user.dto.CreateUserRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserRepositoryTest {
  private UserRepository repository;
  @BeforeEach
  void setUp(){
    repository = new UserRepository();
  }


  @Test
  void saveTest(){
    CreateUserRequest request = new CreateUserRequest("minsu","조조민수");

    User saved = repository.save(request);
    assertEquals(1L, saved.getId());
    assertEquals("minsu", saved.getUserName());
    assertEquals("조조민수", saved.getNickname());
  }

  @Test
  void findAllTest(){
    repository.save(new CreateUserRequest("minsu","조조민수"));
    repository.save(new CreateUserRequest("wpdnjs","제원"));

    List<User> users =  repository.findAll();

    assertEquals(2, users.size());
    assertEquals("minsu", users.getFirst().getUserName());
    assertEquals("조조민수", users.getFirst().getNickname());
    assertEquals("wpdnjs", users.get(1).getUserName());
    assertEquals("제원", users.get(1).getNickname());
  }

  @Test
  void findByIdTest(){
    repository.save(new CreateUserRequest("minsu","조조민수"));
    repository.save(new CreateUserRequest("wpdnjs","제원"));

    Optional<User> result =  repository.findById(1L);

    assertTrue(result.isPresent());

    User user = result.get();

    assertEquals(1L, user.getId());
    assertEquals("minsu", user.getUserName());
    assertEquals("조조민수", user.getNickname());
  }

  @Test
  void findByIdEmptyTest(){
    repository.save(new CreateUserRequest("minsu","조조민수"));
    repository.save(new CreateUserRequest("wpdnjs","제원"));

    assertTrue(repository.findById(3L).isEmpty());
  }


}
