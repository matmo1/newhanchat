package com.newhan.chatservice.dto.userdtos;

import jakarta.validation.constraints.NotBlank;

public record UserLoginDTO(
    @NotBlank String username,
    @NotBlank String password
) 
{}
