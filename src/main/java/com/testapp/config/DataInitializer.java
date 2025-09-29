package com.testapp.config;

import com.testapp.model.User;
import com.testapp.repository.UserRepository;
import com.testapp.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private PostService postService;
    
    @Override
    public void run(String... args) throws Exception {
        // Create 5 test users only if they don't exist
        String[] users = {"mert", "doğukan", "yalman", "arda", "ceren"};
        
        for (String username : users) {
            if (!userRepository.findByName(username).isPresent()) {
                User user = new User(username, passwordEncoder.encode("password123"));
                userRepository.save(user);
                System.out.println("✅ Created user: " + username + " with password: password123");
            } else {
                System.out.println("ℹ️ User already exists: " + username);
            }
        }
        
        // Show total user count
        long userCount = userRepository.count();
        System.out.println("📊 Total users in database: " + userCount);
        
        // Fix empty images in posts
        System.out.println("🔧 Fixing empty images in posts...");
        postService.fixEmptyImages();
    }
}
