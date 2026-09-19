package com.jordi.wolves.wolves_api.game.mapper;

import com.jordi.wolves.wolves_api.game.dto.GameDtoResponse;
import com.jordi.wolves.wolves_api.game.enums.GameStatus;
import com.jordi.wolves.wolves_api.game.model.Game;
import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameMapperTest {

    private GameMapper gameMapper;
    private Game game;

    @BeforeEach
    void setUp() {
        gameMapper = new GameMapper();
        game = new Game("player-1", Difficulty.EASY, List.of(), 1500);
        game.setId("game-1");
        game.setStatus(GameStatus.IN_PROGRESS);
    }

    @Test
    void toDtoMapsGameWithoutMessage() {
        GameDtoResponse result = gameMapper.toDto(game);

        assertEquals("game-1", result.id());
        assertEquals("player-1", result.playerId());
        assertEquals(Difficulty.EASY, result.difficulty());
        assertEquals(GameStatus.IN_PROGRESS, result.status());
        assertNull(result.message());
    }

    @Test
    void toDtoMapsGameWithMessage() {
        GameDtoResponse result = gameMapper.toDto(game, "Partida reanudada");

        assertEquals("game-1", result.id());
        assertEquals("player-1", result.playerId());
        assertEquals(Difficulty.EASY, result.difficulty());
        assertEquals(GameStatus.IN_PROGRESS, result.status());
        assertEquals("Partida reanudada", result.message());
    }
}
