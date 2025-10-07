package com.testapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Integer likeCount = 0;
    
    @Column(nullable = false)
    private Integer dislikeCount = 0;
    
    @Column(nullable = false)
    private Integer saveCount = 0;
    
    public Post() {}
    
    public Post(String prompt, String imageUrl, User user) {
        this.prompt = prompt;
        this.imageUrl = imageUrl;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.likeCount = 0;
        this.dislikeCount = 0;
        this.saveCount = 0;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    
    public Integer getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(Integer dislikeCount) { this.dislikeCount = dislikeCount; }
    
    public Integer getSaveCount() { return saveCount; }
    public void setSaveCount(Integer saveCount) { this.saveCount = saveCount; }
}
