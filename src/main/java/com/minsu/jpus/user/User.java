package com.minsu.jpus.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="users")
public class User
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true)
  private String username;
  @Column(nullable = false)
  private String nickname;
  @Column(nullable = false)
  private String passwordHash;


  public Long getId() {
    return id;
  }
  public String getUsername() {
    return username;
  }
  public String getNickname() {
    return nickname;
  }
  public String getPasswordHash() { return passwordHash; }

  protected User() {
  }

  public User(String username, String passwordHash,String nickname) {
    this.username=username;
    this.passwordHash = passwordHash;
    this.nickname=nickname;
  }
}
