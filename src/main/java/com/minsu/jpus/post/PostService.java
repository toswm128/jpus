package com.minsu.jpus.post;


import com.minsu.jpus.comment.CommentRepository;
import com.minsu.jpus.comment.dto.CommentCountProjection;
import com.minsu.jpus.persona.Persona;
import com.minsu.jpus.persona.PersonaNotFoundException;
import com.minsu.jpus.persona.PersonaRepository;
import com.minsu.jpus.post.dto.CreatePostRequest;
import com.minsu.jpus.post.dto.PostResponse;
import com.minsu.jpus.post.dto.PostSummaryResponse;
import com.minsu.jpus.post.dto.UpdatePostRequest;
import com.minsu.jpus.reaction.ReactionRepository;
import com.minsu.jpus.reaction.dto.ReactionCountProjection;
import com.minsu.jpus.user.User;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class PostService {

  private final PostRepository postRepository;
  private final PersonaRepository personaRepository;
  private final CommentRepository commentRepository;
  private final ReactionRepository reactionRepository;

  public PostService(
      PostRepository postRepository,
      PersonaRepository personaRepository,
      CommentRepository commentRepository,
      ReactionRepository reactionRepository
  ) {
    this.postRepository = postRepository;
    this.personaRepository = personaRepository;
    this.commentRepository = commentRepository;
    this.reactionRepository = reactionRepository;
  }

  private Map<Long, PostSummaryResponse> buildResponses(
      List<Post> posts,
      Long userId
  ) {
    if (posts.isEmpty()) {
      return Map.of();
    }

    List<Long> postIds = posts.stream()
        .map(Post::getId)
        .toList();

    Map<Long, Long> commentCounts =
        commentRepository.countCommentsByPostIds(postIds).stream()
            .collect(Collectors.toMap(
                CommentCountProjection::getPostId,
                CommentCountProjection::getCommentCount
            ));

    Map<Long, ReactionCountProjection> reactionCounts =
        reactionRepository.countReactionsByPostIds(postIds, userId).stream()
            .collect(Collectors.toMap(
                ReactionCountProjection::getPostId,
                row -> row
            ));

    Map<Long, PostSummaryResponse> responses = new HashMap<>();

    for (Post post : posts) {
      ReactionCountProjection reaction = reactionCounts.get(post.getId());

      responses.put(post.getId(), PostSummaryResponse.from(
          post,
          commentCounts.getOrDefault(post.getId(), 0L),
          reaction == null ? 0L : reaction.getReactionCount(),
          reaction != null && reaction.getMyReactionCount() > 0
      ));
    }

    return responses;
  }

  public PostResponse createPost(Long userId, CreatePostRequest request) {
    Persona persona = personaRepository.findByIdAndUser_IdAndDeletedAtIsNull(request.personaId(),
        userId).orElseThrow(
        () -> new PersonaNotFoundException("persona id", request.personaId().toString())
    );

    User user = persona.getUser();
    return PostResponse.from(
        postRepository.save(new Post(user, persona, request.title(), request.content())));
  }

  public Page<PostSummaryResponse> getPosts(int page, int size, Long userId) {
    Page<Post> posts =
        postRepository.findAllByDeletedAtIsNullOrderByCreatedAtDescIdDesc(
            PageRequest.of(page, size)
        );

    Map<Long, PostSummaryResponse> responses =
        buildResponses(posts.getContent(), userId);

    return posts.map(post -> responses.get(post.getId()));
  }

  public PostSummaryResponse getPost(Long postId, Long userId) {
    Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
        .orElseThrow(() -> new PostNotFoundException("post id", postId));

    return buildResponses(List.of(post), userId).get(postId);
  }

  @Transactional
  public void deletePost(Long postId, Long userId) {
    Post post = postRepository.findByIdAndUser_IdAndDeletedAtIsNull(postId, userId)
        .orElseThrow(() -> new PostNotFoundException("post id", postId.toString()));

    post.delete();
  }

  @Transactional
  public PostResponse patchPost(
      Long postId,
      Long userId,
      UpdatePostRequest request
  ) {
    Post post =
        postRepository.findByIdAndUser_IdAndDeletedAtIsNull(postId, userId).orElseThrow(
            () -> new PostNotFoundException("post id", postId.toString())
        );

    post.updateContent(request.title(), request.content());
    return PostResponse.from(post);
  }
}
