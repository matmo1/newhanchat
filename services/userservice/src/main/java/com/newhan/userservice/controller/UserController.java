package com.newhan.userservice.controller;

import com.newhan.userservice.dto.*;
import com.newhan.userservice.model.User;
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

    public UserController(UserService userService) {
        this.userService = userService;
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
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable String id) {
        // Pure delegation to the service layer
        return ResponseEntity.ok(userService.getProfileDTO(id));
    }
    
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        // Pure delegation to the service layer
        return ResponseEntity.ok(userService.getAllUsersDTO());
    }

    @PostMapping(value = "/{id}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDTO> uploadProfilePicture(
            @PathVariable String id, 
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        
        // Pure delegation to the service layer
        return ResponseEntity.ok(userService.updateProfilePicture(id, file, request));
    }

    @PatchMapping("/{id}/bio")
    public ResponseEntity<UserResponseDTO> updateBio(@PathVariable String id, @RequestBody String newBio) {
        // Pure delegation to the service layer
        return ResponseEntity.ok(userService.updateBio(id, newBio));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<UserResponseDTO> updateName(
            @PathVariable String id, 
            @RequestParam String fname, 
            @RequestParam String lname) {
        
        // Pure delegation to the service layer
        return ResponseEntity.ok(userService.updateName(id, fname, lname));
}