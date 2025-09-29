package com.testapp.controller;

import com.testapp.dto.CreatePostRequest;
import com.testapp.dto.PostResponse;
import com.testapp.model.User;
import com.testapp.service.PostService;
import com.testapp.service.UserService;
import com.testapp.service.GeminiService;
import com.testapp.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostController {
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private GeminiService geminiService;
    
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        try {
            List<PostResponse> posts = postService.getAllPosts();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest request, 
                                      @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.badRequest().body("Invalid token");
            }
            
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByName(username).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            PostResponse post = postService.createPost(request.getPrompt(), user);
            return ResponseEntity.ok(post);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create post: " + e.getMessage());
        }
    }
    
    @PostMapping("/fix-images")
    public ResponseEntity<?> fixEmptyImages() {
        try {
            postService.fixEmptyImages();
            return ResponseEntity.ok("Images fixed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to fix images: " + e.getMessage());
        }
    }
    
    @GetMapping("/test-gemini")
    public ResponseEntity<?> testGemini() {
        try {
            System.out.println("🧪 Gemini test endpoint çağrıldı");
            String result = geminiService.testGeminiConnection();
            System.out.println("🧪 Gemini test sonucu: " + result);
            return ResponseEntity.ok("Gemini test result: " + result);
        } catch (Exception e) {
            System.err.println("❌ Gemini test hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Gemini test failed: " + e.getMessage());
        }
    }
}
