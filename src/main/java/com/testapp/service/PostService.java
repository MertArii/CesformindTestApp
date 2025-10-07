package com.testapp.service;

import com.testapp.dto.PostResponse;
import com.testapp.model.Post;
import com.testapp.model.User;
import com.testapp.model.UserPostInteraction;
import com.testapp.repository.PostRepository;
import com.testapp.service.UserInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private GeminiService geminiService;
    
    @Autowired
    private UserInteractionService userInteractionService;

    public List<PostResponse> getAllPosts() {
        return getAllPosts(null);
    }
    
    public List<PostResponse> getAllPosts(Long userId) {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        
        // Boş imageUrl'leri düzelt
        boolean needsUpdate = false;
        for (Post post : posts) {
            if (post.getImageUrl() == null || post.getImageUrl().isEmpty()) {
                String placeholderUrl = getQuickPlaceholder(post.getPrompt());
                post.setImageUrl(placeholderUrl);
                postRepository.save(post);
                needsUpdate = true;
                System.out.println("✅ Post " + post.getId() + " resmi düzeltildi: " + placeholderUrl);
            }
        }
        
        if (needsUpdate) {
            System.out.println("🎉 Boş resimler düzeltildi!");
        }
        
        return posts.stream()
                .map(post -> convertToResponse(post, userId))
                .collect(Collectors.toList());
    }

    /**
     * HIZLI POST OLUŞTURMA - Önce placeholder ile post oluştur, sonra güncelle
     */
    public PostResponse createPost(String prompt, User user) {
        try {
            // 1. Hızlı placeholder ile post oluştur
            String placeholderUrl = getQuickPlaceholder(prompt);
            Post post = new Post(prompt, placeholderUrl, user);
            Post savedPost = postRepository.save(post);

            // 2. Arka planda gerçek resmi oluştur
            updatePostImageAsync(savedPost.getId(), prompt);

            // 3. Hemen response dön (kullanıcı beklemez)
            return convertToResponse(savedPost, user.getId());

        } catch (Exception e) {
            System.err.println("Post oluşturma hatası: " + e.getMessage());
            e.printStackTrace();

            // Hata durumunda da post oluştur
            String errorUrl = "https://via.placeholder.com/800x600/FF6B6B/FFFFFF?text=Image+Generation+Failed";
            Post errorPost = new Post(prompt, errorUrl, user);
            Post savedErrorPost = postRepository.save(errorPost);
            return convertToResponse(savedErrorPost, user.getId());
        }
    }

    /**
     * Asenkron resim güncelleme
     */
    @Async
    public void updatePostImageAsync(Long postId, String prompt) {
        try {
            String imageUrl;
            
            // Prompt boyutuna göre farklı stratejiler kullan
            if (prompt.length() > 100000) {
                System.out.println("🚨 Çok büyük prompt tespit edildi, özel işlem yapılıyor");
                imageUrl = geminiService.generateImageForLargePrompt(prompt);
            } else if (prompt.length() > 10000) {
                System.out.println("⚠️ Büyük prompt tespit edildi, kısa versiyon kullanılıyor");
                imageUrl = geminiService.generateImageWithShortPrompt(prompt);
            } else {
                imageUrl = geminiService.generateImage(prompt);
            }

            // Post'u güncelle
            postRepository.findById(postId).ifPresent(post -> {
                post.setImageUrl(imageUrl);
                postRepository.save(post);
                System.out.println("✅ Post " + postId + " resmi güncellendi: " + imageUrl);
            });

        } catch (Exception e) {
            System.err.println("❌ Post " + postId + " resmi güncellenemedi: " + e.getMessage());
        }
    }

    /**
     * Hızlı placeholder URL oluştur
     */
    private String getQuickPlaceholder(String prompt) {
        try {
            String text = prompt.length() > 30 ? prompt.substring(0, 30) + "..." : prompt;
            String encodedText = java.net.URLEncoder.encode(text, "UTF-8");

            // Renkli ve çekici placeholder
            String[] colors = {"4A90E2", "50C878", "FF6B6B", "FFD93D", "A569BD", "48C9B0"};
            String randomColor = colors[(int) (Math.random() * colors.length)];

            return String.format("https://via.placeholder.com/800x600/%s/FFFFFF?text=%s",
                    randomColor, encodedText);
        } catch (Exception e) {
            return "https://via.placeholder.com/800x600/4A90E2/FFFFFF?text=Generating...";
        }
    }

    /**
     * Alternatif - Tamamen senkron ama timeout'lu versiyon
     */
    public PostResponse createPostSync(String prompt, User user) {
        try {
            // Timeout ile resim üret (max 10 saniye bekle)
            String imageUrl = generateImageWithTimeout(prompt, 10);
            Post post = new Post(prompt, imageUrl, user);
            Post savedPost = postRepository.save(post);
            return convertToResponse(savedPost, user.getId());

        } catch (Exception e) {
            System.err.println("Senkron post oluşturma hatası: " + e.getMessage());
            String fallbackUrl = getQuickPlaceholder(prompt);
            Post post = new Post(prompt, fallbackUrl, user);
            Post savedPost = postRepository.save(post);
            return convertToResponse(savedPost, user.getId());
        }
    }

    /**
     * Timeout'lu resim üretimi
     */
    private String generateImageWithTimeout(String prompt, int timeoutSeconds) {
        try {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                // Prompt boyutuna göre uygun metodu seç
                if (prompt.length() > 100000) {
                    return geminiService.generateImageForLargePrompt(prompt);
                } else if (prompt.length() > 10000) {
                    return geminiService.generateImageWithShortPrompt(prompt);
                } else {
                    return geminiService.generateImage(prompt);
                }
            });

            return future.get(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("⏰ Resim üretimi timeout'a uğradı, placeholder kullanılıyor");
            return getQuickPlaceholder(prompt);
        }
    }

    /**
     * Boş imageUrl'leri düzelt
     */
    public void fixEmptyImages() {
        try {
            List<Post> postsWithEmptyImages = postRepository.findAll()
                    .stream()
                    .filter(post -> post.getImageUrl() == null || post.getImageUrl().isEmpty())
                    .collect(Collectors.toList());
            
            for (Post post : postsWithEmptyImages) {
                String placeholderUrl = getQuickPlaceholder(post.getPrompt());
                post.setImageUrl(placeholderUrl);
                postRepository.save(post);
                System.out.println("✅ Post " + post.getId() + " resmi düzeltildi: " + placeholderUrl);
            }
            
            System.out.println("🎉 Toplam " + postsWithEmptyImages.size() + " post düzeltildi");
        } catch (Exception e) {
            System.err.println("❌ Resim düzeltme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<PostResponse> getSavedPosts(Long userId) {
        System.out.println("💾 PostService.getSavedPosts called - User: " + userId);
        
        List<Post> savedPosts = userInteractionService.getSavedPosts(userId);
        
        return savedPosts.stream()
                .map(post -> convertToResponse(post, userId))
                .collect(Collectors.toList());
    }
    
    /**
     * Delete a post - only the owner can delete their own post
     */
    public boolean deletePost(Long postId, Long userId) {
        try {
            System.out.println("🗑️ Delete request for post ID: " + postId + " by user ID: " + userId);
            
            // Find the post
            Post post = postRepository.findById(postId).orElse(null);
            if (post == null) {
                System.out.println("❌ Post not found with ID: " + postId);
                return false;
            }
            
            // Check if the user is the owner of the post
            if (!post.getUser().getId().equals(userId)) {
                System.out.println("❌ User " + userId + " is not the owner of post " + postId + ". Owner is: " + post.getUser().getId());
                return false;
            }
            
            // Delete the post
            postRepository.delete(post);
            System.out.println("✅ Post " + postId + " deleted successfully by user " + userId);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error deleting post " + postId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private PostResponse convertToResponse(Post post) {
        return convertToResponse(post, null);
    }
    
    private PostResponse convertToResponse(Post post, Long userId) {
        PostResponse response = new PostResponse(
                post.getId(),
                post.getPrompt(),
                post.getImageUrl(),
                post.getUser().getName(),
                post.getCreatedAt()
        );
        
        // Set interaction counts
        response.setLikeCount(post.getLikeCount());
        response.setDislikeCount(post.getDislikeCount());
        response.setSaveCount(post.getSaveCount());
        
        // Set user interaction status if userId is provided
        if (userId != null) {
            UserPostInteraction interaction = userInteractionService.getUserInteraction(userId, post.getId());
            if (interaction != null) {
                response.setIsLiked(interaction.getIsLiked());
                response.setIsDisliked(interaction.getIsDisliked());
                response.setIsSaved(interaction.getIsSaved());
            } else {
                response.setIsLiked(false);
                response.setIsDisliked(false);
                response.setIsSaved(false);
            }
        } else {
            response.setIsLiked(false);
            response.setIsDisliked(false);
            response.setIsSaved(false);
        }
        
        return response;
    }
}