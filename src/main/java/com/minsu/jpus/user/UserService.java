package com.minsu.jpus.user;

import com.minsu.jpus.user.dto.CreateUserRequest;
import com.minsu.jpus.user.dto.UserResponse;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public UserResponse createUser(CreateUserRequest request){
    if(userRepository.existsByUsername(request.username())) {
      throw new UserDuplicateException("이미 존재하는 아이디입니다.");
    }
      User user = new User(request.username(), passwordEncoder.encode(request.password()),request.nickname());
      return UserResponse.from(userRepository.save(user));

  }

  public List<UserResponse> getUsers(){
    return userRepository.findAll().stream().map(UserResponse::from).toList();
  }

  public UserResponse getUser(Long id){
    User user = userRepository.findById(id).orElseThrow(()->
        new UserNotFoundException("존재하지 않는 유저입니다. id="+id)
    );

    return UserResponse.from(user);

  }
}
