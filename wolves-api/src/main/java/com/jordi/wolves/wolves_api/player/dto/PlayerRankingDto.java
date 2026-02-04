package com.jordi.wolves.wolves_api.player.dto;

public record PlayerRankingDto(
        String playerId,
        String name,
        int money
) {}