package com.jordi.wolves.wolves_api.game.dto;

import com.jordi.wolves.wolves_api.game.enums.GameStatus;
import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import com.jordi.wolves.wolves_api.question.model.Question;

import java.util.List;

public record GameDtoResponse(
        String id,
        String playerId,
        Difficulty difficulty,
        GameStatus status,
        int currentQuestionIndex,
        int score

) {
}
