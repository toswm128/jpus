package com.minsu.jpus.reaction;

import com.minsu.jpus.post.Post;
import com.minsu.jpus.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "reaction",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_reaction_user_post",
        columnNames = {"user_id", "post_id"}
    ))
public class Reaction {

  protected Reaction() {

  }

  public Reaction(User user, Post post) {
    this.user = user;
    this.post = post;
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public Post getPost() {
    return post;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  Post post;

  @Column(nullable = false)
  LocalDateTime createdAt;


}
