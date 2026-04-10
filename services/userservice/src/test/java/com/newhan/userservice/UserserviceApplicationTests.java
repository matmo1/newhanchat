package com.newhan.userservice;

import com.newhan.userservice.dto.UserLoginDTO;
import com.newhan.userservice.dto.UserRegistrationDTO;
import com.newhan.userservice.model.User;
import com.newhan.userservice.repository.UserRepository;
import com.newhan.userservice.service.JwtService;
import com.newhan.userservice.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegistrationDTO regDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("uuid-123");
        testUser.setUsername("johndoe");
        testUser.setPassword("hashed_password");

        // Assumes your DTO is a record mapping exactly to this
        regDto = new UserRegistrationDTO("johndoe", "John", "Doe", LocalDateTime.now(), "password123");
    }

    @Test
    void register_Success_HashesPasswordAndSaves() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        
        assertDoesNotThrow(() -> userService.register(regDto));
        
        verify(userRepository).save(argThat(user -> 
            user.getUsername().equals("johndoe") && 
            user.getPassword().equals("hashed_password")
        ));
    }

    @Test
    void register_ThrowsException_IfUserAlreadyExists() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(regDto));
        assertEquals("Username already exists", exception.getMessage());
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success_ReturnsJwtToken() {
        UserLoginDTO loginDto = new UserLoginDTO("johndoe", "password123");
        
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtService.generateToken("johndoe", "uuid-123")).thenReturn("mocked-jwt-token");

        String token = userService.login(loginDto);

        assertEquals("mocked-jwt-token", token);
        verify(userRepository).save(testUser); // Verify lastSeen is updated and saved
    }

    @Test
    void login_ThrowsException_IfUserNotFound() {
        UserLoginDTO loginDto = new UserLoginDTO("unknown", "password");
        
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.login(loginDto));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void login_ThrowsException_IfPasswordInvalid() {
        UserLoginDTO loginDto = new UserLoginDTO("johndoe", "wrongpassword");
        
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "hashed_password")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.login(loginDto));
        assertEquals("Invalid credentials", exception.getMessage());
    }
}