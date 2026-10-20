package com.minsu.jpus.comment;

import com.minsu.jpus.persona.Persona;
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
import java.time.LocalDateTime;

@Entity
@Table(name="comment")
public class Comment {

  protected Comment(){

  }

  public Comment(Post post, User user, Persona persona, String content) {
    this.post = post;
    this.user = user;
    this.authorNameSnapshot = persona.getDisplayName();
    this.persona = persona;
    this.content = content;
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public Post getPost() {
    return post;
  }

  public User getUser() {
    return user;
  }

  public Persona getPersona() {
    return persona;
  }

  public String getAuthorNameSnapshot() {
    return authorNameSnapshot;
  }

  public String getContent() {
    return content;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "post_id",nullable = false)
  private Post post;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "persona_id", nullable = false)
  Persona persona;

  @Column(nullable = false)
  private String authorNameSnapshot;

  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  LocalDateTime createdAt;

  @Column(nullable = true)
  LocalDateTime updatedAt;

  @Column(nullable = true)
  LocalDateTime deletedAt;

}
