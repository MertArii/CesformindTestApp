package com.testapp.controller;

import com.testapp.dto.CreatePostRequest;
import com.testapp.dto.PostResponse;
import com.testapp.model.User;
import com.testapp.service.PostService;
import com.testapp.service.UserService;
import com.testapp.service.UserInteractionService;
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
    
    @Autowired
    private UserInteractionService userInteractionService;
    
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String cleanToken = token.substring(7);
                if (jwtUtil.validateToken(cleanToken)) {
                    String username = jwtUtil.getUsernameFromToken(cleanToken);
                    User user = userService.findByName(username).orElse(null);
                    if (user != null) {
                        userId = user.getId();
                    }
                }
            }
            
            List<PostResponse> posts = postService.getAllPosts(userId);
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
    
    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable Long postId, 
                                    @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            System.out.println("🔥 Like request received for post ID: " + postId);
            
            // Extract user from JWT token
            User user = null;
            if (token != null && token.startsWith("Bearer ")) {
                String cleanToken = token.substring(7);
                if (jwtUtil.validateToken(cleanToken)) {
                    String username = jwtUtil.getUsernameFromToken(cleanToken);
                    user = userService.findByName(username).orElse(null);
                    System.out.println("👤 Authenticated user: " + (user != null ? user.getName() : "null"));
                }
            }
            
            // Fallback to default user if no valid token
            if (user == null) {
                System.out.println("⚠️ No valid token, using default user");
                user = userService.findByName("user1").orElse(null);
                if (user == null) {
                    System.out.println("❌ Default user not found, creating one");
                    user = new User("user1", "password123");
                    user = userService.save(user);
                }
            }
            
            System.out.println("👤 Using user: " + user.getName());
            
            System.out.println("📡 Calling likePost service for user " + user.getId() + " and post " + postId);
            boolean success = userInteractionService.likePost(user.getId(), postId);
            System.out.println("✅ Like result: " + success);
            
            if (success) {
                return ResponseEntity.ok("Post liked/unliked successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to like post");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Like error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to like post: " + e.getMessage());
        }
    }
    
    @PostMapping("/{postId}/dislike")
    public ResponseEntity<?> dislikePost(@PathVariable Long postId, 
                                       @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            System.out.println("👎 Dislike request received for post ID: " + postId);
            
            // Extract user from JWT token
            User user = null;
            if (token != null && token.startsWith("Bearer ")) {
                String cleanToken = token.substring(7);
                if (jwtUtil.validateToken(cleanToken)) {
                    String username = jwtUtil.getUsernameFromToken(cleanToken);
                    user = userService.findByName(username).orElse(null);
                    System.out.println("👤 Authenticated user: " + (user != null ? user.getName() : "null"));
                }
            }
            
            // Fallback to default user if no valid token
            if (user == null) {
                System.out.println("⚠️ No valid token, using default user");
                user = userService.findByName("user1").orElse(null);
                if (user == null) {
                    System.out.println("❌ Default user not found, creating one");
                    user = new User("user1", "password123");
                    user = userService.save(user);
                }
            }
            
            System.out.println("👤 Using user: " + user.getName());
            
            System.out.println("📡 Calling dislikePost service for user " + user.getId() + " and post " + postId);
            boolean success = userInteractionService.dislikePost(user.getId(), postId);
            System.out.println("✅ Dislike result: " + success);
            
            if (success) {
                return ResponseEntity.ok("Post disliked/undisliked successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to dislike post");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Dislike error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to dislike post: " + e.getMessage());
        }
    }
    
    @PostMapping("/{postId}/save")
    public ResponseEntity<?> savePost(@PathVariable Long postId, 
                                     @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            System.out.println("💾 Save request received for post ID: " + postId);
            
            // Extract user from JWT token
            User user = null;
            if (token != null && token.startsWith("Bearer ")) {
                String cleanToken = token.substring(7);
                if (jwtUtil.validateToken(cleanToken)) {
                    String username = jwtUtil.getUsernameFromToken(cleanToken);
                    user = userService.findByName(username).orElse(null);
                    System.out.println("👤 Authenticated user: " + (user != null ? user.getName() : "null"));
                }
            }
            
            // Fallback to default user if no valid token
            if (user == null) {
                System.out.println("⚠️ No valid token, using default user");
                user = userService.findByName("user1").orElse(null);
                if (user == null) {
                    System.out.println("❌ Default user not found, creating one");
                    user = new User("user1", "password123");
                    user = userService.save(user);
                }
            }
            
            System.out.println("👤 Using user: " + user.getName());
            
            System.out.println("📡 Calling savePost service for user " + user.getId() + " and post " + postId);
            boolean success = userInteractionService.savePost(user.getId(), postId);
            System.out.println("✅ Save result: " + success);
            
            if (success) {
                return ResponseEntity.ok("Post saved/unsaved successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to save post");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Save error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to save post: " + e.getMessage());
        }
    }
    
    @GetMapping("/saved")
    public ResponseEntity<List<PostResponse>> getSavedPosts(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            System.out.println("💾 Saved posts request received");
            
            // Extract user from JWT token
            User user = null;
            if (token != null && token.startsWith("Bearer ")) {
                String cleanToken = token.substring(7);
                if (jwtUtil.validateToken(cleanToken)) {
                    String username = jwtUtil.getUsernameFromToken(cleanToken);
                    user = userService.findByName(username).orElse(null);
                    System.out.println("👤 Authenticated user: " + (user != null ? user.getName() : "null"));
                }
            }
            
            // Fallback to default user if no valid token
            if (user == null) {
                System.out.println("⚠️ No valid token, using default user");
                user = userService.findByName("user1").orElse(null);
                if (user == null) {
                    System.out.println("❌ Default user not found, creating one");
                    user = new User("user1", "password123");
                    user = userService.save(user);
                }
            }
            
            System.out.println("👤 Using user: " + user.getName());
            
            List<PostResponse> savedPosts = postService.getSavedPosts(user.getId());
            System.out.println("✅ Found " + savedPosts.size() + " saved posts");
            
            return ResponseEntity.ok(savedPosts);
            
        } catch (Exception e) {
            System.out.println("❌ Saved posts error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, 
                                       @RequestHeader("Authorization") String token) {
        try {
            System.out.println("🗑️ Delete post request received for post ID: " + postId);
            
            // Extract user from JWT token
            User user = null;
            if (token.startsWith("Bearer ")) {
                String cleanToken = token.substring(7);
                if (jwtUtil.validateToken(cleanToken)) {
                    String username = jwtUtil.getUsernameFromToken(cleanToken);
                    user = userService.findByName(username).orElse(null);
                    System.out.println("👤 Authenticated user: " + (user != null ? user.getName() : "null"));
                }
            }
            
            if (user == null) {
                System.out.println("❌ No valid token provided");
                return ResponseEntity.badRequest().body("Authentication required");
            }
            
            System.out.println("👤 Using user: " + user.getName() + " (ID: " + user.getId() + ")");
            
            // Call the service to delete the post
            boolean success = postService.deletePost(postId, user.getId());
            
            if (success) {
                System.out.println("✅ Post " + postId + " deleted successfully");
                return ResponseEntity.ok("Post deleted successfully");
            } else {
                System.out.println("❌ Failed to delete post " + postId);
                return ResponseEntity.badRequest().body("Failed to delete post. You can only delete your own posts.");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Delete post error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to delete post: " + e.getMessage());
        }
    }
}
