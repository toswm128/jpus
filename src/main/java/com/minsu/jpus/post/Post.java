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
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

@Entity
@Table(name="post")
public class Post {

  public Post(User user, Persona persona,String content) {
    this.user = user;
    this.persona = persona;
    this.authorNameSnapshot = persona.getDisplayName();
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
  Long id;
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @ManyToOne
  @JoinColumn(name = "persona_id", nullable = false)
  Persona persona;

  @Column(nullable = false)
  String authorNameSnapshot;

  @Column(nullable = false)
  String content;

  @Column(nullable = false)
  LocalDateTime createdAt;

  @Column(nullable = true)
  LocalDateTime updatedAt;

  @Column(nullable = true)
  LocalDateTime deletedAt;

  public void delete(){
    this.deletedAt = LocalDateTime.now();
  }
  public void updateContent(String content){
    this.content = content;
    this.updatedAt = LocalDateTime.now();
  }
}
