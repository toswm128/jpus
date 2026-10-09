package com.minsu.jpus.user;

public class User
{


  private Long id;
  private String userName;


  private String nickname;

  public Long getId() {
    return id;
  }
  public String getUsername() {
    return userName;
  }
  public String getNickname() {
    return nickname;
  }

  public User(long sequence, String username, String nickname) {
    this.id=sequence;
    this.userName=username;
    this.nickname=nickname;
  }
}
