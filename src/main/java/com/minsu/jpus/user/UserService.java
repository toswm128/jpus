package com.minsu.jpus.user;

import com.minsu.jpus.user.dto.CreateUserRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User createUser(CreateUserRequest request){
    return userRepository.save(request);
  }

  public List<User> getUsers(){
    return userRepository.findAll();
  }

  public User getUser(Long id){
    return userRepository.findById(id).orElseThrow(()->
        new UserNotFoundException("존재하지 않는 유저입니다. id="+id));
  }
}
