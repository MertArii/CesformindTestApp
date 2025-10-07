package com.testapp.repository;

import com.testapp.model.Post;
import com.testapp.model.User;
import com.testapp.model.UserPostInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPostInteractionRepository extends JpaRepository<UserPostInteraction, Long> {
    Optional<UserPostInteraction> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);
    List<UserPostInteraction> findByUserAndIsSavedTrueOrderByCreatedAtDesc(User user);
}
