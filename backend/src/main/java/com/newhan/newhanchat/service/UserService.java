package com.newhan.newhanchat.service;

import org.bson.types.ObjectId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newhan.newhanchat.dto.JwtResponseDTO;
import com.newhan.newhanchat.dto.userdtos.UserLoginDTO;
import com.newhan.newhanchat.dto.userdtos.UserRegistrationDTO;
import com.newhan.newhanchat.dto.userdtos.UserResponseDTO;
import com.newhan.newhanchat.model.user.StatusType;
import com.newhan.newhanchat.model.user.User;
import com.newhan.newhanchat.repository.UserRepository;

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

    @Transactional
    public UserResponseDTO registerUser(UserRegistrationDTO dto) {
        if (userRepository.existsByUserName(dto.userName())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUserName(dto.userName());
        user.setFirstName(dto.fname());
        user.setLastName(dto.lname());
        user.setDateOfBirth(dto.dOfBirth());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.getUserStatus().setStatus(StatusType.OFFLINE);

        User savedUser = userRepository.save(user);
        return toDto(savedUser);
    }

    @Transactional
    public JwtResponseDTO loginUser(UserLoginDTO dto) {
        User user = userRepository.findByUserName(dto.username())
            .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = jwtService.generateToken(user);
        
        // DO THIS: Passing (token, userId, username) to match the Record definition
        return new JwtResponseDTO(token, user.getUserId().toString(), user.getUserName());
    }

    public UserResponseDTO getUserById(ObjectId id) {
        return userRepository.findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public UserResponseDTO updateStatus(ObjectId id, StatusType status) {
        User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.getUserStatus().setStatus(status);
        return toDto(userRepository.save(user));
    }

    public java.util.List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public void connectUser(String userId) {
        if (userId == null) return;
        userRepository.findById(new ObjectId(userId)).ifPresent(user -> {
            user.getUserStatus().setOnline(); // Restores preference
            userRepository.save(user);
        });
    }

    @Transactional
    public void disconnectUser(String userId) {
        if (userId == null) return;
        userRepository.findById(new ObjectId(userId)).ifPresent(user -> {
            user.getUserStatus().setOffline(); // Sets OFFLINE but keeps preference
            userRepository.save(user);
        });
    }

    private UserResponseDTO toDto(User user) {
        return new UserResponseDTO(user.getUserId(), 
            user.getUserName(), 
            user.getFirstName(), 
            user.getLastName(), 
            user.getUserStatus());
    }
}
