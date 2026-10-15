package com.minsu.jpus.post;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
  List<Post> findAllByDeletedAtIsNullOrderByCreatedAtDesc();
  Optional<Post> findByIdAndDeletedAtIsNull(Long id);
  Optional<Post> findByIdAndUser_IdAndDeletedAtIsNull(Long postId,Long userId);
}
