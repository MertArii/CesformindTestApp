package com.testapp.dto;

import java.time.LocalDateTime;

public class PostResponse {
    private Long id;
    private String prompt;
    private String imageUrl;
    private String userName;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer saveCount;
    private Boolean isLiked;
    private Boolean isDisliked;
    private Boolean isSaved;
    
    public PostResponse() {}
    
    public PostResponse(Long id, String prompt, String imageUrl, String userName, LocalDateTime createdAt) {
        this.id = id;
        this.prompt = prompt;
        this.imageUrl = imageUrl;
        this.userName = userName;
        this.createdAt = createdAt;
        this.likeCount = 0;
        this.dislikeCount = 0;
        this.saveCount = 0;
        this.isLiked = false;
        this.isDisliked = false;
        this.isSaved = false;
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
    
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    
    public Integer getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(Integer dislikeCount) { this.dislikeCount = dislikeCount; }
    
    public Integer getSaveCount() { return saveCount; }
    public void setSaveCount(Integer saveCount) { this.saveCount = saveCount; }
    
    public Boolean getIsLiked() { return isLiked; }
    public void setIsLiked(Boolean isLiked) { this.isLiked = isLiked; }
    
    public Boolean getIsDisliked() { return isDisliked; }
    public void setIsDisliked(Boolean isDisliked) { this.isDisliked = isDisliked; }
    
    public Boolean getIsSaved() { return isSaved; }
    public void setIsSaved(Boolean isSaved) { this.isSaved = isSaved; }
}
