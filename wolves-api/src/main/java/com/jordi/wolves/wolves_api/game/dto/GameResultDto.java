package com.jordi.wolves.wolves_api.game.dto;

public record GameResultDto(
        String gameId,
        String playerId,
        int score,
        int totalQuestions,
        boolean passed,
        int reward,
        String finalMessage
) {}