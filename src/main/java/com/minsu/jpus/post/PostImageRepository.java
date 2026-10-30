package com.minsu.jpus.post;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository
    extends JpaRepository<PostImage, Long> {

  List<PostImage> findAllByPost_IdOrderBySortOrderAscIdAsc(
      Long postId
  );

  Optional<PostImage> findByIdAndPost_IdAndPost_DeletedAtIsNull(
      Long imageId,
      Long postId
  );
}