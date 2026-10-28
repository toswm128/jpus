package com.minsu.jpus.reaction;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

  List<Reaction> findAllByPost_Id(Long postId);

  void deleteByPost_IdAndUser_Id(Long postId, Long UserId);
}
