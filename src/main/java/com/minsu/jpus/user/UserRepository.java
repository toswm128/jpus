package com.minsu.jpus.user;

import com.minsu.jpus.user.dto.CreateUserRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
  private final List<User> users = new ArrayList<>();
  private long sequence = 1L;

  public User save(CreateUserRequest request){
    User user = new User(sequence++,request.username(),request.nickname());
    users.add(user);
    return user;
  }

  public List<User> findAll(){
    return List.copyOf(users);
  }

  public Optional<User> findById(Long id){
    return users.stream().filter(u-> Objects.equals(u.getId(), id)).findFirst();
  }
}
