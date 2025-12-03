package com.newhan.chatservice.service; // Adjust to match your folder structure

import com.newhan.chatservice.dto.userdtos.UserLoginDTO;
import com.newhan.chatservice.dto.userdtos.UserRegistrationDTO;
import com.newhan.chatservice.dto.userdtos.UserResponseDTO;
import com.newhan.chatservice.dto.JwtResponseDTO; // Import this!
import com.newhan.chatservice.model.user.User;
import com.newhan.chatservice.repository.UserRepository;
import org.bson.types.ObjectId;
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

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(new ObjectId());
        testUser.setUserName("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
    }

    @Test
    void registerUser_Success() {
        UserRegistrationDTO dto = new UserRegistrationDTO(
                "testuser", "Test", "User", LocalDateTime.now(), "password123");

        when(userRepository.existsByUserName(dto.userName())).thenReturn(false);
        when(passwordEncoder.encode(dto.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDTO result = userService.registerUser(dto);

        assertNotNull(result);
        assertEquals("testuser", result.username());
    }

    @Test
    void registerUser_ThrowsException_WhenUsernameExists() {
        UserRegistrationDTO dto = new UserRegistrationDTO(
                "testuser", "Test", "User", LocalDateTime.now(), "password123");

        when(userRepository.existsByUserName(dto.userName())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }

    @Test
    void loginUser_Success() {
        UserLoginDTO loginDTO = new UserLoginDTO("testuser", "password123");
        
        when(userRepository.findByUserName(loginDTO.username())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginDTO.password(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("mock-jwt-token");

        // FIX: Expect JwtResponseDTO, not String
        JwtResponseDTO response = userService.loginUser(loginDTO);

        // FIX: Check the token property
        assertEquals("mock-jwt-token", response.token());
    }

    @Test
    void loginUser_ThrowsException_WhenPasswordInvalid() {
        UserLoginDTO loginDTO = new UserLoginDTO("testuser", "wrongpassword");

        when(userRepository.findByUserName(loginDTO.username())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginDTO.password(), testUser.getPassword())).thenReturn(false);

        // FIX: Ensure this is wrapped in assertThrows
        assertThrows(IllegalArgumentException.class, () -> userService.loginUser(loginDTO));
    }
}