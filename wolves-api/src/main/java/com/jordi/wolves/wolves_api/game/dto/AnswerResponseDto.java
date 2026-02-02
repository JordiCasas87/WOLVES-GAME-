package com.jordi.wolves.wolves_api.game.dto;

public record AnswerResponseDto(
        boolean correct,
        String wolfMessage
) {
}
