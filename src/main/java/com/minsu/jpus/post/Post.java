package com.minsu.jpus.post;

import com.minsu.jpus.persona.Persona;
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
@Table(name = "post")
public class Post {

  public Post(User user, Persona persona, String title, String content) {
    this.user = user;
    this.persona = persona;
    this.authorNameSnapshot = persona.getDisplayName();
    this.title = title;
    this.content = content;
    this.createdAt = LocalDateTime.now();
  }

  protected Post() {
  }

  public Long getId() {
    return id;
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

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }

  public String getContentJson() {
    return contentJson;
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
  Long id;
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @ManyToOne
  @JoinColumn(name = "persona_id", nullable = false)
  Persona persona;

  @Column(nullable = false)
  String authorNameSnapshot;

  @Column(length = 100, nullable = true)
  String title;

  @Column(nullable = false, columnDefinition = "LONGTEXT")
  String content;

  @Column(columnDefinition = "LONGTEXT")
  String contentJson;

  @Column(nullable = false)
  LocalDateTime createdAt;

  @Column(nullable = true)
  LocalDateTime updatedAt;

  @Column(nullable = true)
  LocalDateTime deletedAt;

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }

  public void updateContent(String title, String content) {
    this.title = title;
    this.content = content;
    this.updatedAt = LocalDateTime.now();
  }

  public void initializeRichContent(String content, String contentJson) {
    this.content = content;
    this.contentJson = contentJson;
  }

  public void updateRichContent(
      String title,
      String content,
      String contentJson
  ) {
    this.title = title;
    this.content = content;
    this.contentJson = contentJson;
    this.updatedAt = LocalDateTime.now();
  }
}
