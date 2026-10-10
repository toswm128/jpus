package com.minsu.jpus.user;

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
  private String username;
  private String nickname;


  public Long getId() {
    return id;
  }
  public String getUsername() {
    return username;
  }
  public String getNickname() {
    return nickname;
  }

  protected User() {
  }

  public User(String username, String nickname) {
    this.username=username;
    this.nickname=nickname;
  }
}
