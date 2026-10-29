package com.minsu.jpus.comment;

import com.minsu.jpus.comment.dto.CommentCountProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findAllByPost_IdAndDeletedAtIsNullOrderByCreatedAtAsc(Long postId);

  Optional<Comment> findByIdAndPost_IdAndUser_IdAndDeletedAtIsNull(Long commentId, Long postId,
      Long userId);

  Optional<Comment> findByIdAndPost_IdAndUser_IdAndDeletedAtIsNullAndPost_DeletedAtIsNull(
      Long commentId, Long postId, Long userId);

  @Query("""
      select c.post.id as postId, count(c) as commentCount
      from Comment c
      where c.post.id in :postIds
        and c.deletedAt is null
      group by c.post.id
      """)
  List<CommentCountProjection> countCommentsByPostIds(
      @Param("postIds") List<Long> postIds
  );
}