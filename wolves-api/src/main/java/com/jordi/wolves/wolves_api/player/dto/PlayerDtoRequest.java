package com.jordi.wolves.wolves_api.player.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PlayerDtoRequest(

        @NotBlank(message = "Player name cannot be empty")
        String name,
        @NotNull(message = "Player must have age")
        int age
) {
}
