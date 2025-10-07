package com.testapp.service;

import com.testapp.model.Post;
import com.testapp.model.User;
import com.testapp.model.UserPostInteraction;
import com.testapp.repository.PostRepository;
import com.testapp.repository.UserPostInteractionRepository;
import com.testapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class UserInteractionService {
    
    @Autowired
    private UserPostInteractionRepository interactionRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public boolean likePost(Long userId, Long postId) {
        System.out.println("🔥 UserInteractionService.likePost called - User: " + userId + ", Post: " + postId);
        
        User user = userRepository.findById(userId).orElse(null);
        Post post = postRepository.findById(postId).orElse(null);
        
        if (user == null || post == null) {
            System.out.println("❌ User or Post not found - User: " + (user != null) + ", Post: " + (post != null));
            return false;
        }
        
        System.out.println("✅ User and Post found - User: " + user.getName() + ", Post: " + post.getId());
        
        UserPostInteraction interaction = interactionRepository.findByUserAndPost(user, post)
                .orElse(new UserPostInteraction(user, post));
        
        System.out.println("📊 Current interaction - Liked: " + interaction.getIsLiked() + ", Disliked: " + interaction.getIsDisliked());
        System.out.println("📊 Current counts - Likes: " + post.getLikeCount() + ", Dislikes: " + post.getDislikeCount());
        
        if (interaction.getIsLiked()) {
            // Unlike
            System.out.println("👎 Unliking post");
            interaction.setIsLiked(false);
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            // Like
            System.out.println("👍 Liking post");
            if (interaction.getIsDisliked()) {
                // Remove dislike first
                System.out.println("🔄 Removing dislike first");
                interaction.setIsDisliked(false);
                post.setDislikeCount(post.getDislikeCount() - 1);
            }
            interaction.setIsLiked(true);
            post.setLikeCount(post.getLikeCount() + 1);
        }
        
        System.out.println("💾 Saving interaction and post");
        interactionRepository.save(interaction);
        postRepository.save(post);
        
        System.out.println("✅ Like operation completed - New counts - Likes: " + post.getLikeCount() + ", Dislikes: " + post.getDislikeCount());
        return true;
    }
    
    @Transactional
    public boolean dislikePost(Long userId, Long postId) {
        System.out.println("👎 UserInteractionService.dislikePost called - User: " + userId + ", Post: " + postId);
        
        User user = userRepository.findById(userId).orElse(null);
        Post post = postRepository.findById(postId).orElse(null);
        
        if (user == null || post == null) {
            System.out.println("❌ User or Post not found - User: " + (user != null) + ", Post: " + (post != null));
            return false;
        }
        
        System.out.println("✅ User and Post found - User: " + user.getName() + ", Post: " + post.getId());
        
        UserPostInteraction interaction = interactionRepository.findByUserAndPost(user, post)
                .orElse(new UserPostInteraction(user, post));
        
        System.out.println("📊 Current interaction - Liked: " + interaction.getIsLiked() + ", Disliked: " + interaction.getIsDisliked());
        System.out.println("📊 Current counts - Likes: " + post.getLikeCount() + ", Dislikes: " + post.getDislikeCount());
        
        if (interaction.getIsDisliked()) {
            // Remove dislike
            System.out.println("👍 Removing dislike");
            interaction.setIsDisliked(false);
            post.setDislikeCount(post.getDislikeCount() - 1);
        } else {
            // Dislike
            System.out.println("👎 Disliking post");
            if (interaction.getIsLiked()) {
                // Remove like first
                System.out.println("🔄 Removing like first");
                interaction.setIsLiked(false);
                post.setLikeCount(post.getLikeCount() - 1);
            }
            interaction.setIsDisliked(true);
            post.setDislikeCount(post.getDislikeCount() + 1);
        }
        
        System.out.println("💾 Saving interaction and post");
        interactionRepository.save(interaction);
        postRepository.save(post);
        
        System.out.println("✅ Dislike operation completed - New counts - Likes: " + post.getLikeCount() + ", Dislikes: " + post.getDislikeCount());
        return true;
    }
    
    @Transactional
    public boolean savePost(Long userId, Long postId) {
        System.out.println("💾 UserInteractionService.savePost called - User: " + userId + ", Post: " + postId);
        
        User user = userRepository.findById(userId).orElse(null);
        Post post = postRepository.findById(postId).orElse(null);
        
        if (user == null || post == null) {
            System.out.println("❌ User or Post not found - User: " + (user != null) + ", Post: " + (post != null));
            return false;
        }
        
        System.out.println("✅ User and Post found - User: " + user.getName() + ", Post: " + post.getId());
        
        UserPostInteraction interaction = interactionRepository.findByUserAndPost(user, post)
                .orElse(new UserPostInteraction(user, post));
        
        System.out.println("📊 Current interaction - Saved: " + interaction.getIsSaved());
        System.out.println("📊 Current counts - Saves: " + post.getSaveCount());
        
        if (interaction.getIsSaved()) {
            // Unsave
            System.out.println("📤 Unsaving post");
            interaction.setIsSaved(false);
            post.setSaveCount(post.getSaveCount() - 1);
        } else {
            // Save
            System.out.println("💾 Saving post");
            interaction.setIsSaved(true);
            post.setSaveCount(post.getSaveCount() + 1);
        }
        
        System.out.println("💾 Saving interaction and post");
        interactionRepository.save(interaction);
        postRepository.save(post);
        
        System.out.println("✅ Save operation completed - New count - Saves: " + post.getSaveCount());
        return true;
    }
    
    public UserPostInteraction getUserInteraction(Long userId, Long postId) {
        User user = userRepository.findById(userId).orElse(null);
        Post post = postRepository.findById(postId).orElse(null);
        
        if (user == null || post == null) {
            return null;
        }
        
        return interactionRepository.findByUserAndPost(user, post).orElse(null);
    }
    
    public List<Post> getSavedPosts(Long userId) {
        System.out.println("💾 UserInteractionService.getSavedPosts called - User: " + userId);
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            System.out.println("❌ User not found");
            return List.of();
        }
        
        List<UserPostInteraction> savedInteractions = interactionRepository
                .findByUserAndIsSavedTrueOrderByCreatedAtDesc(user);
        
        List<Post> savedPosts = savedInteractions.stream()
                .map(UserPostInteraction::getPost)
                .toList();
        
        System.out.println("✅ Found " + savedPosts.size() + " saved posts for user " + user.getName());
        return savedPosts;
    }
}
