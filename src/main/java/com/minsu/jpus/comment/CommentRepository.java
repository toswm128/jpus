package com.minsu.jpus.comment;

import com.minsu.jpus.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
