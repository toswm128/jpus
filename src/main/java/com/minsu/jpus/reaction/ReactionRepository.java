package com.minsu.jpus.reaction;

import com.minsu.jpus.reaction.dto.ReactionCountProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

  List<Reaction> findAllByPost_Id(Long postId);

  void deleteByPost_IdAndUser_Id(Long postId, Long UserId);

  @Query("""
      select r.post.id as postId,
             count(r) as reactionCount,
             sum(case when r.user.id = :userId then 1 else 0 end)
                 as myReactionCount
      from Reaction r
      where r.post.id in :postIds
      group by r.post.id
      """)
  List<ReactionCountProjection> countReactionsByPostIds(
      @Param("postIds") List<Long> postIds,
      @Param("userId") Long userId
  );
}
