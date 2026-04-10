package com.newhan.postservice.controller;

import com.newhan.postservice.model.Post;
import com.newhan.postservice.service.FileStorageService;
import com.newhan.postservice.service.PostService;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



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
            System.err.println("❌ UPLOAD ERROR: " + e.getMessage());
            e.printStackTrace(); 
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Post> createPost(
            @RequestBody PostRequest request,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        
        // ✨ FIXED: Passed in the exact order the Service expects them!
        Post createdPost = postService.createPost(
            userId, 
            request.getAuthorName(), 
            request.getAuthorProfilePic(), 
            request.getContent(), 
            request.getImageUrl()
        );
        return ResponseEntity.ok(createdPost);
    }

    @GetMapping
    public ResponseEntity<Page<Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Controller just passes the raw numbers to the Service
        return ResponseEntity.ok(postService.getAllPosts(page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, Authentication authentication) {
            try {
                postService.deletePost(id, authentication.getName());
                return ResponseEntity.ok().build();
            } catch (RuntimeException e) {
                return ResponseEntity.status(403).body(e.getMessage());
            }
        }
    public static class PostRequest {
        private String content;
        private String imageUrl;
        private String authorName;       // ✨ NEW
        private String authorProfilePic; // ✨ NEW
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getAuthorName() { return authorName; }
        public void setAuthorName(String authorName) { this.authorName = authorName; }
        public String getAuthorProfilePic() { return authorProfilePic; }
        public void setAuthorProfilePic(String authorProfilePic) { this.authorProfilePic = authorProfilePic; }
    }
}