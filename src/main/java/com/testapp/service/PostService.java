package com.testapp.service;

import com.testapp.dto.PostResponse;
import com.testapp.model.Post;
import com.testapp.model.User;
import com.testapp.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private GeminiService geminiService;
    
    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public PostResponse createPost(String prompt, User user) {
        String imageUrl = geminiService.generateImage(prompt);
        Post post = new Post(prompt, imageUrl, user);
        Post savedPost = postRepository.save(post);
        return convertToResponse(savedPost);
    }
    
    private PostResponse convertToResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getPrompt(),
                post.getImageUrl(),
                post.getUser().getName(),
                post.getCreatedAt()
        );
    }
}
