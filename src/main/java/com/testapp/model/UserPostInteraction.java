package com.testapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_post_interactions")
public class UserPostInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    @Column(nullable = false)
    private Boolean isLiked = false;
    
    @Column(nullable = false)
    private Boolean isDisliked = false;
    
    @Column(nullable = false)
    private Boolean isSaved = false;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    public UserPostInteraction() {}
    
    public UserPostInteraction(User user, Post post) {
        this.user = user;
        this.post = post;
        this.isLiked = false;
        this.isDisliked = false;
        this.isSaved = false;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    
    public Boolean getIsLiked() { return isLiked; }
    public void setIsLiked(Boolean isLiked) { this.isLiked = isLiked; }
    
    public Boolean getIsDisliked() { return isDisliked; }
    public void setIsDisliked(Boolean isDisliked) { this.isDisliked = isDisliked; }
    
    public Boolean getIsSaved() { return isSaved; }
    public void setIsSaved(Boolean isSaved) { this.isSaved = isSaved; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
