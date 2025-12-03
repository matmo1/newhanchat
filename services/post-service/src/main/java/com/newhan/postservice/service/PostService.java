package com.newhan.postservice.service;

import com.newhan.postservice.model.Post;
import com.newhan.postservice.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post createPost(String userId, String content, String imageUrl) {
        // Create the object here (Business Logic)
        Post post = new Post(userId, content, imageUrl);
        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public List<Post> getUserPosts(String userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}