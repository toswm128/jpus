package com.minsu.jpus.post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "post_image")
public class PostImage {

  public Long getId() {
    return id;
  }

  public Post getPost() {
    return post;
  }

  public String getStorageKey() {
    return storageKey;
  }

  public String getContentType() {
    return contentType;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @Column(nullable = false, unique = true)
  private String storageKey;

  @Column(nullable = false)
  private String contentType;

  @Column(nullable = false)
  private int sortOrder;

  protected PostImage() {
  }

  public PostImage(
      Post post,
      String storageKey,
      String contentType,
      int sortOrder
  ) {
    this.post = post;
    this.storageKey = storageKey;
    this.contentType = contentType;
    this.sortOrder = sortOrder;
  }

}

