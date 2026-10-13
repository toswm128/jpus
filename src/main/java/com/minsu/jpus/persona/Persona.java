package com.minsu.jpus.persona;

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
@Table(name="persona")
public class Persona {
  protected Persona() {
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private String displayName;
  private String profileImageKey;
  private String bio;
  private LocalDateTime deletedAt;

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }

  public void changeDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Persona(User user,String displayName,String bio){
    this.user=user;
    this.displayName = displayName;
    this.bio = bio;
  }

  public Long getId() {
    return id;
  }
  public User getUser() {
    return user;
  }
  public String getDisplayName() {
    return displayName;
  }

  public String getProfileImageKey() {
    return profileImageKey;
  }

  public String getBio() {
    return bio;
  }

  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }

  public void updateProfile(String displayName,String bio){
    if(displayName != null){
      this.displayName = displayName;
    }
    if(bio!=null){
      this.bio = bio;
    }
  }
}
