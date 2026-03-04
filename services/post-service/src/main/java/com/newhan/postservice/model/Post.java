package com.newhan.postservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The MongoDB ID of the user who posted this
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl; // URL/Path to the image (not the image itself)

    private LocalDateTime createdAt;

    // --- Constructors ---
    public Post() {}

    public Post(String userId, String content, String imageUrl) {
        this.userId = userId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}