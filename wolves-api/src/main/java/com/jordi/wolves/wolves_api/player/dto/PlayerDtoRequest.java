package com.jordi.wolves.wolves_api.player.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record PlayerDtoRequest(

        String name,
        String password,
        int age

) {
}
