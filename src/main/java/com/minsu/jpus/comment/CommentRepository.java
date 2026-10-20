package com.minsu.jpus.comment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findAllByPost_IdAndDeletedAtIsNull(Long postId);
}