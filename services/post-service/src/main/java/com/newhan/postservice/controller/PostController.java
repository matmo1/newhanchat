package com.newhan.postservice.controller;

import com.newhan.postservice.model.Post;
import com.newhan.postservice.service.PostService; // Import Service
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService; // Use Service, not Repository

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody PostRequest request) {
        // Delegate to service
        Post createdPost = postService.createPost(
            request.getUserId(), 
            request.getContent(), 
            request.getImageUrl()
        );
        return ResponseEntity.ok(createdPost);
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/user/{userId}")
    public List<Post> getUserPosts(@PathVariable String userId) {
        return postService.getUserPosts(userId);
    }

    // (Keep your static PostRequest class here as before)
    public static class PostRequest {
        private String userId;
        private String content;
        private String imageUrl;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}