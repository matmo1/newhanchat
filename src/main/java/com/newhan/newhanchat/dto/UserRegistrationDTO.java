package com.newhan.newhanchat.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.*;

public record UserRegistrationDTO(
    @NotBlank
    @Size(min = 3, max = 20)
    String userName,
    @NotBlank
    String fname,
    @NotBlank
    String lname,
    @NotBlank
    LocalDateTime dOfBirth,
    @NotBlank
    @Size(min = 8)
    String password
) {}
