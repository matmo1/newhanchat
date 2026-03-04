package com.newhanchat.postservice.service;

import com.newhan.postservice.model.Post;
import com.newhan.postservice.repository.PostRepository;
import com.newhan.postservice.service.PostService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private Post testPost;

    @BeforeEach
    void setUp() {
        // Setup a dummy post for testing
        testPost = new Post("user123", "Hello World", "http://image.url");
        testPost.setId(1L);
    }

    @Test
    void createPost_Success() {
        // Arrange
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // Act
        Post result = postService.createPost("user123", "Hello World", "http://image.url");

        // Assert
        assertNotNull(result);
        assertEquals("user123", result.getUserId());
        assertEquals("Hello World", result.getContent());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void getAllPosts_Success() {
        // Arrange
        when(postRepository.findAll()).thenReturn(List.of(testPost));

        // Act
        List<Post> result = postService.getAllPosts();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(postRepository).findAll();
    }

    @Test
    void getUserPosts_Success() {
        // Arrange
        String userId = "user123";
        when(postRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(testPost));

        // Act
        List<Post> result = postService.getUserPosts(userId);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals("user123", result.get(0).getUserId());
        verify(postRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }
}