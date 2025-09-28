package com.testapp.dto;

public class CreatePostRequest {
    private String prompt;
    
    public CreatePostRequest() {}
    
    public CreatePostRequest(String prompt) {
        this.prompt = prompt;
    }
    
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
}
