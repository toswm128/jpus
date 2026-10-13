package com.minsu.jpus.post;


import com.minsu.jpus.persona.Persona;
import com.minsu.jpus.persona.PersonaNotFoundException;
import com.minsu.jpus.persona.PersonaRepository;
import com.minsu.jpus.post.dto.CreatePostRequest;
import com.minsu.jpus.post.dto.PostResponse;
import com.minsu.jpus.user.User;
import com.minsu.jpus.user.UserNotFoundException;
import com.minsu.jpus.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class PostService {
  private final PostRepository postRepository;
  private final PersonaRepository personaRepository;
  private final UserRepository userRepository;

  public PostService(PostRepository postRepository, PersonaRepository personaRepository,
      UserRepository userRepository) {
    this.postRepository = postRepository;
    this.personaRepository = personaRepository;
    this.userRepository = userRepository;
  }

  public PostResponse createPost(Long userId,  CreatePostRequest request  ){
    User user  = userRepository.findById(userId).orElseThrow(
        ()->new UserNotFoundException("user id",userId.toString())
    );
    Persona persona = personaRepository.findById(request.personaId()).orElseThrow(
        ()-> new PersonaNotFoundException("persona id",request.personaId().toString())
    );

    return PostResponse.from(postRepository.save(new Post(user,persona,request.content() )));
  }
}
