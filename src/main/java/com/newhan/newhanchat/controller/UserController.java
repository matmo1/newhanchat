package com.newhan.newhanchat.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newhan.newhanchat.dto.userdtos.UserRegistrationDTO;
import com.newhan.newhanchat.dto.userdtos.UserResponseDTO;
import com.newhan.newhanchat.model.user.StatusType;
import com.newhan.newhanchat.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserResponseDTO register(@RequestBody UserRegistrationDTO dto) {
        return userService.registerUser(dto);
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUser(@PathVariable ObjectId id) {
        return userService.getUserById(id);
    }
    
    @PatchMapping("/{id}/status")
    public UserResponseDTO updateStatus(
        @PathVariable ObjectId id,
        @RequestParam StatusType status
        ) {
            return userService.updateStatus(id, status);
        }
    
}
