package com.newhan.postservice.controller;

import com.newhan.postservice.model.Post;
import com.newhan.postservice.service.FileStorageService;
import com.newhan.postservice.service.PostService; // Import Service

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final FileStorageService fileStorageService;

    public PostController(PostService postService, FileStorageService fileStorageService) {
        this.postService = postService;
        this.fileStorageService = fileStorageService;
    }

    // 1. Endpoint to Upload Image
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileStorageService.saveFile(file);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed");
        }
    }

    @PostMapping
    public ResponseEntity<Post> createPost(
            @RequestBody PostRequest request,
            Authentication authentication // Inject Authentication
    ) {
        // SECURE: Get userId from the token, not the request body!
        String userId = authentication.getName();
        
        Post createdPost = postService.createPost(
            userId, // Use the verified ID
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

    public static class PostRequest {
        // Remove userId from here, we don't need the client to send it anymore
        // private String userId; 
        private String content;
        private String imageUrl;
        
        // Remove userId getters/setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}