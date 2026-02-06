package com.newhan.userservice.controller;

import com.newhan.userservice.dto.*;
import com.newhan.userservice.model.User;
import com.newhan.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        User user = userService.getProfile(userService.getProfile(dto.username()).getId()); 
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
}