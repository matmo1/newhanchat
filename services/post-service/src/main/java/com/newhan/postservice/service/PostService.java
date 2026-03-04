package com.newhan.postservice.service;

import com.newhan.postservice.model.Post;
import com.newhan.postservice.repository.PostRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<Post> getAllPosts(int page, int limit) {
        // Sort by newest first
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findAll(pageable);
    }

    public List<Post> getUserPosts(String userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void deletePost(Long postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post does't exist"));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Not your post");
        }

        postRepository.delete(post);
    }
}