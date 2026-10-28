package com.minsu.jpus.reaction;

import com.minsu.jpus.post.Post;
import com.minsu.jpus.post.PostNotFoundException;
import com.minsu.jpus.post.PostRepository;
import com.minsu.jpus.reaction.dto.ReactionResponse;
import com.minsu.jpus.user.User;
import com.minsu.jpus.user.UserNotFoundException;
import com.minsu.jpus.user.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReactionService {

  private final ReactionRepository reactionRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  public ReactionService(ReactionRepository reactionRepository, PostRepository postRepository,
      UserRepository userRepository) {
    this.reactionRepository = reactionRepository;
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  public ReactionResponse getReactions(
      Long userId,
      Long postId
  ) {
    List<Reaction> response = reactionRepository.findAllByPost_Id(postId);

    return ReactionResponse.from(response, userId);
  }

  public void reactionPost(
      Long userId,
      Long postId
  ) {
    Post post = postRepository.findByIdAndDeletedAtIsNull(postId).orElseThrow(
        () -> new PostNotFoundException("post_id", postId)
    );

    User user = userRepository.findById(userId).orElseThrow(
        () -> new UserNotFoundException("user_id", userId.toString())
    );

    reactionRepository.save(new Reaction(user, post));
  }

  @Transactional
  public void deleteReactions(
      Long userId,
      Long postId
  ) {
    reactionRepository.deleteByPost_IdAndUser_Id(postId, userId);
  }


}
