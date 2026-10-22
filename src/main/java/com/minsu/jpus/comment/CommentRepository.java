package com.minsu.jpus.comment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findAllByPost_IdAndDeletedAtIsNullOrderByCreatedAtAsc(Long postId);
  Optional<Comment> findByIdAndPost_IdAndUser_IdAndDeletedAtIsNull(Long commentId,Long postId,Long userId);
  Optional<Comment> findByIdAndPost_IdAndUser_IdAndDeletedAtIsNullAndPost_DeletedAtIsNull(Long commentId,Long postId,Long userId);

}