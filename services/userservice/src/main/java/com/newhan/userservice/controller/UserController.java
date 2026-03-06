package com.newhan.userservice.controller;

import com.newhan.userservice.dto.*;
import com.newhan.userservice.model.User;
import com.newhan.userservice.service.ProfilePicStorageService;
import com.newhan.userservice.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ProfilePicStorageService picStorageService;

    public UserController(UserService userService, ProfilePicStorageService picStorageService) {
        this.userService = userService;
        this.picStorageService = picStorageService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDTO dto) {
        userService.register(dto);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@RequestBody UserLoginDTO dto) {
        String token = userService.login(dto);
        // Fetch user to return ID and username
        User user = userService.getUserByUsername(dto.username());
        return ResponseEntity.ok(new JwtResponseDTO(token, user.getId(), user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping(value = "/{id}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDTO> uploadProfilePicture(
            @PathVariable String id, 
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        
        // Pure delegation. The service handles the heavy lifting.
        User updatedUser = userService.updateProfilePicture(id, file, request);
        
        return ResponseEntity.ok(mapToDTO(updatedUser));
    }

    private UserResponseDTO mapToDTO(User user) {
        return new UserResponseDTO(
            user.getId(), user.getUsername(), user.getFirstName(), 
            user.getLastName(), user.getStatus(), user.getBio(), user.getProfilePictureUrl()
        );
    }
}