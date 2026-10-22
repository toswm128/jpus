package com.minsu.jpus.comment;

import com.minsu.jpus.comment.dto.CommentResponse;
import com.minsu.jpus.comment.dto.CreateCommentRequest;
import com.minsu.jpus.comment.dto.UpdateCommentRequest;
import com.minsu.jpus.persona.Persona;
import com.minsu.jpus.persona.PersonaNotFoundException;
import com.minsu.jpus.persona.PersonaRepository;
import com.minsu.jpus.post.Post;
import com.minsu.jpus.post.PostNotFoundException;
import com.minsu.jpus.post.PostRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final PersonaRepository personaRepository;

  public CommentService(CommentRepository commentRepository, PostRepository postRepository,
      PersonaRepository personaRepository) {
    this.commentRepository = commentRepository;
    this.postRepository = postRepository;
    this.personaRepository = personaRepository;
  }

  public CommentResponse createComment(
      Long postId,
      Long userId,
      CreateCommentRequest request
  ) {
    Post post = postRepository.findByIdAndDeletedAtIsNull(postId).orElseThrow(
        () -> new PostNotFoundException("post_id", postId.toString())
    );
    Persona persona = personaRepository.findByIdAndUser_IdAndDeletedAtIsNull(request.personaId(),
        userId).orElseThrow(
        () -> new PersonaNotFoundException("persona_id", request.personaId().toString())
    );

    Comment comment = new Comment(post, persona.getUser(), persona, request.content());

    commentRepository.save(comment);
    return CommentResponse.from(comment);
  }

  public List<CommentResponse> getComments(Long postId) {
    postRepository.findByIdAndDeletedAtIsNull(postId).orElseThrow(
        () -> new PostNotFoundException("post_id", postId)
    );
    return commentRepository.findAllByPost_IdAndDeletedAtIsNullOrderByCreatedAtAsc(postId)
        .stream()
        .map(CommentResponse::from)
        .toList();
  }

  @Transactional
  public CommentResponse patchComment(
      Long commentId,
      Long postId,
      Long userId,
      UpdateCommentRequest request
  ) {
    Comment comment = commentRepository.findByIdAndPost_IdAndUser_IdAndDeletedAtIsNullAndPost_DeletedAtIsNull(commentId,
        postId, userId).orElseThrow(
        () -> new CommentNotFoundException("commentId", commentId)
    );

    comment.updateComment(request.content());
    return CommentResponse.from(comment);
  }

  @Transactional
  public void deleteComment(
      Long commentId,
      Long postId,
      Long userId
  ) {
    Comment comment = commentRepository.findByIdAndPost_IdAndUser_IdAndDeletedAtIsNullAndPost_DeletedAtIsNull(commentId,
        postId, userId).orElseThrow(
        () -> new CommentNotFoundException("commentId", commentId)
    );

    comment.deleteComment();
  }
}
