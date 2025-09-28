package com.testapp.dto;

import java.time.LocalDateTime;

public class PostResponse {
    private Long id;
    private String prompt;
    private String imageUrl;
    private String userName;
    private LocalDateTime createdAt;
    
    public PostResponse() {}
    
    public PostResponse(Long id, String prompt, String imageUrl, String userName, LocalDateTime createdAt) {
        this.id = id;
        this.prompt = prompt;
        this.imageUrl = imageUrl;
        this.userName = userName;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
