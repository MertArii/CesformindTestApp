package com.testapp.controller;

import com.testapp.dto.LoginRequest;
import com.testapp.dto.LoginResponse;
import com.testapp.model.User;
import com.testapp.service.UserService;
import com.testapp.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest registerRequest) {
        try {
            // Check if user already exists
            if (userService.findByName(registerRequest.getName()).isPresent()) {
                return ResponseEntity.badRequest().body("User already exists");
            }
            
            // Create new user
            User user = new User(registerRequest.getName(), registerRequest.getPassword());
            User savedUser = userService.save(user);
            
            String token = jwtUtil.generateToken(savedUser.getName());
            return ResponseEntity.ok(new LoginResponse(token, savedUser.getName()));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.authenticate(loginRequest.getName(), loginRequest.getPassword())
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("Invalid credentials");
            }
            
            String token = jwtUtil.generateToken(user.getName());
            return ResponseEntity.ok(new LoginResponse(token, user.getName()));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }
}
