package com.jordi.wolves.wolves_api.player.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record PlayerDtoRequest(

        @NotBlank(message = "Name is required")
        String name,
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,
        @Min(value = 0, message = "Age must be positive")
        int age

) {
}
