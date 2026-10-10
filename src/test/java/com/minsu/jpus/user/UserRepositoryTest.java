package com.minsu.jpus.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;


  @Test
  void saveTest(){
    User request = new User("minsu","test-hash","조조민수");

    User user = userRepository.save(request);
    assertNotNull(user.getId());
    assertEquals("minsu", user.getUsername());
    assertEquals("조조민수", user.getNickname());
    assertNotNull(user.getId());
  }

  @Test
  void findAllTest(){
    userRepository.save(new User("minsu","test-hash","조조민수"));
    userRepository.save(new User("wpdnjs","test-hash","제원"));

    List<User> users =  userRepository.findAll();

    assertEquals(2, users.size());
  }

  @Test
  void findByIdTest(){
    Long userId = userRepository.save(new User("minsu","test-hash","조조민수")).getId();
    userRepository.save(new User("wpdnjs","test-hash","제원"));

    Optional<User> result =  userRepository.findById(userId);

    assertTrue(result.isPresent());

    User user = result.orElseThrow();

    assertNotNull(user.getId());
    assertEquals("minsu", user.getUsername());
    assertEquals("조조민수", user.getNickname());
  }

  @Test
  void findByUsernameTest(){
    userRepository.save(new User("minsu","test-hash","조조민수"));

    User user = userRepository.findByUsername("minsu").orElseThrow();

    assertNotNull(user.getId());
    assertEquals("minsu", user.getUsername());
  }

  @Test
  void findByIdEmptyTest(){
    assertTrue(userRepository.findById(3L).isEmpty());
  }

  @Test
  void existsByUsernameTest() {
    userRepository.save(new User("minsu","test-hash", "조조민수"));

    assertTrue(userRepository.existsByUsername("minsu"));
    assertFalse(userRepository.existsByUsername("unknown"));
  }

}
