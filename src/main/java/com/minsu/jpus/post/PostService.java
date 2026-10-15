package com.minsu.jpus.post;


import com.minsu.jpus.persona.Persona;
import com.minsu.jpus.persona.PersonaNotFoundException;
import com.minsu.jpus.persona.PersonaRepository;
import com.minsu.jpus.persona.dto.PersonaResponse;
import com.minsu.jpus.persona.dto.UpdatePersonaRequest;
import com.minsu.jpus.post.dto.CreatePostRequest;
import com.minsu.jpus.post.dto.PostResponse;
import com.minsu.jpus.post.dto.UpdatePostRequest;
import com.minsu.jpus.user.User;
import com.minsu.jpus.user.UserNotFoundException;
import com.minsu.jpus.user.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
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

  public List<PostResponse> getPosts(){
    List<PostResponse> posts = postRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc()
        .stream()
        .map(PostResponse::from)
        .toList();

    return posts;
  }

  public PostResponse getPost(Long postId){
    Post post = postRepository.findByIdAndDeletedAtIsNull(postId).orElseThrow(
        () -> new PostNotFoundException("post id",postId.toString())
    );

    return PostResponse.from(post);
  }

  public void deletePost(Long postId,Long userId){
    Post post = postRepository.findByIdAndUser_IdAndDeletedAtIsNull(postId,userId)
        .orElseThrow(()->new PostNotFoundException("post id",postId.toString()));

    post.delete();
    postRepository.save(post);
  }

  @Transactional
  public PostResponse patchPost(
      Long postId,
      Long userId,
      UpdatePostRequest request
  ) {
    Post post =
        postRepository.findByIdAndUser_IdAndDeletedAtIsNull(postId, userId).orElseThrow(
            () -> new PostNotFoundException("persona id", postId.toString())
        );

    post.updateContent(request.content());
    return PostResponse.from(post);
  }
}
