package com.jordi.wolves.wolves_api.security.auth.dtos;

public record LoginRequest(

        String name,
        String password
) { }
