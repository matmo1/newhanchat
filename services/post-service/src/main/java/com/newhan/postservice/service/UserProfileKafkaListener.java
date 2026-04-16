package com.newhan.postservice.service;

import com.newhan.postservice.dto.UserUpdateEvent;
import com.newhan.postservice.model.Post;
import com.newhan.postservice.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserProfileKafkaListener {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileKafkaListener.class);
    private final PostRepository postRepository;

    public UserProfileKafkaListener(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // ⚠️ Make sure "user-profile-updates" matches the EXACT topic name the user-service is sending to!
    @KafkaListener(topics = "user-profile-updates", groupId = "post-service-group")
    public void handleUserProfileUpdate(UserUpdateEvent event) {
        logger.info("Received Profile Update for user: {}", event.userId());

        // ✨ The "Slow/Safe Update" Chunking Mechanism
        int pageSize = 100; // Only pull 100 posts into RAM at a time
        int pageNumber = 0;
        Page<Post> postPage;

        do {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            
            // Assuming event.username() is what you store in Post.userId
            postPage = postRepository.findByUserId(event.userId(), pageable);

            if (postPage.hasContent()) {
                for (Post post : postPage.getContent()) {
                    // Update the details
                    post.setAuthorName(event.fullName());
                    post.setAuthorProfilePic(event.profilePictureUrl());
                }
                // Save the batch of 100 back to the database
                postRepository.saveAll(postPage.getContent());
                logger.info("Updated batch {} of posts for user {}", pageNumber, event.userId());
            }
            
            // Move to the next chunk of 100 posts
            pageNumber++;
            
        } while (postPage.hasNext());

        logger.info("Finished updating all posts for user: {}", event.userId());
    }
}