package com.newhan.userservice.service;

import com.newhan.userservice.dto.UserLoginDTO;
import com.newhan.userservice.dto.UserRegistrationDTO;
import com.newhan.userservice.model.User;
import com.newhan.userservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(UserRegistrationDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setFirstName(dto.getFname());
        user.setLastName(dto.getLname());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // Handle Date parsing carefully in production
        user.setDateOfBirth(LocalDateTime.parse(dto.getDob())); 
        
        userRepository.save(user);
    }

    public String login(UserLoginDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);

        return jwtService.generateToken(user.getUsername(), user.getId());
    }

    public User getProfile(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    // Helper to get ID by Username
    public User getProfileByUsername(String username) {
         return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}