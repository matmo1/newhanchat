package com.newhan.chatservice.service;

import com.newhan.chatservice.model.user.User;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // Inject the secret key manually since we aren't loading the Spring Context
        ReflectionTestUtils.setField(jwtService, "secretKey", 
            "your-super-secret-and-long-key-that-is-not-nyakyv-taen-klyuch"); 
    }

    @Test
    void generateToken_ReturnsValidToken() {
        // Arrange
        User user = new User();
        user.setUserId(new ObjectId());
        user.setUserName("testuser");

        // Act
        String token = jwtService.generateToken(user);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        // You could add logic here to decode the token and verify the subject if needed
    }
}