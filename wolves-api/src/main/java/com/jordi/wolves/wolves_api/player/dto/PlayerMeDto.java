package com.jordi.wolves.wolves_api.player.dto;

public record PlayerMeDto(
        String id,
        String name,
        String role,
        int money,
        int gamesPlayed,
        int level,
        String notes
) {
}
