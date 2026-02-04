package com.jordi.wolves.wolves_api.security.auth.dtos;

public record RegisterRequest(

        String name,
        String password
) {
}
