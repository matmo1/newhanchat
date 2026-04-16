package com.newhan.postservice.repository;

import com.newhan.postservice.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserIdOrderByCreatedAtDesc(String userId);
    
    // ✨ NEW: Used for safely batch-updating thousands of posts without crashing RAM
    Page<Post> findByUserId(String userId, Pageable pageable);
}