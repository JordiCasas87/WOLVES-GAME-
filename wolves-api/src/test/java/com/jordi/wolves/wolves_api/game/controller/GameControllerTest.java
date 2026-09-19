package com.jordi.wolves.wolves_api.game.controller;

import com.jordi.wolves.wolves_api.game.dto.AnswerRequestDto;
import com.jordi.wolves.wolves_api.game.dto.AnswerResponseDto;
import com.jordi.wolves.wolves_api.game.dto.GameDtoResponse;
import com.jordi.wolves.wolves_api.game.dto.GameResultDto;
import com.jordi.wolves.wolves_api.game.enums.GameStatus;
import com.jordi.wolves.wolves_api.game.exception.GameNotFoundException;
import com.jordi.wolves.wolves_api.game.service.GameService;
import com.jordi.wolves.wolves_api.question.dto.QuestionDtoNextResponse;
import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import com.jordi.wolves.wolves_api.security.jwt.JwtAuthenticationFilter;
import com.jordi.wolves.wolves_api.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "jordi", roles = "USER")
class GameControllerTest {

    private final Authentication authenticatedUser = UsernamePasswordAuthenticationToken.authenticated(
            "jordi",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @Test
    void createGameReturnsCreatedGame() throws Exception {
        GameDtoResponse response = new GameDtoResponse(
                "game-1",
                "player-1",
                Difficulty.EASY,
                GameStatus.CREATED,
                null
        );
        when(gameService.createGame(eq(authenticatedUser), eq(Difficulty.EASY)))
                .thenReturn(response);

        mockMvc.perform(post("/game/new")
                        .principal(authenticatedUser)
                        .param("difficulty", "EASY"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("game-1"))
                .andExpect(jsonPath("$.playerId").value("player-1"))
                .andExpect(jsonPath("$.difficulty").value("EASY"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").isEmpty());

        verify(gameService).createGame(authenticatedUser, Difficulty.EASY);
    }

    @Test
    void createGameReturnsBadRequestForInvalidDifficulty() throws Exception {
        mockMvc.perform(post("/game/new").param("difficulty", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verify(gameService, never()).createGame(any(), any());
    }

    @Test
    void nextQuestionReturnsQuestionData() throws Exception {
        QuestionDtoNextResponse response = new QuestionDtoNextResponse(
                1,
                "Introducción",
                "¿Cuál es la respuesta?",
                List.of("A", "B", "C", "D")
        );
        when(gameService.nextQuestion("game-1")).thenReturn(response);

        mockMvc.perform(get("/game/game-1/nextQuestion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberQuestion").value(1))
                .andExpect(jsonPath("$.intro").value("Introducción"))
                .andExpect(jsonPath("$.text").value("¿Cuál es la respuesta?"))
                .andExpect(jsonPath("$.answers.length()").value(4))
                .andExpect(jsonPath("$.answers[0]").value("A"));

        verify(gameService).nextQuestion("game-1");
    }

    @Test
    void answerQuestionReturnsAnswerResult() throws Exception {
        AnswerResponseDto response = new AnswerResponseDto(true, "Correcto, sigamos");
        when(gameService.answerQuestion("game-1", new AnswerRequestDto(1))).thenReturn(response);

        mockMvc.perform(post("/game/game-1/answer")
                        .contentType("application/json")
                        .content("""
                                {
                                  "selectedAnswer": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.wolfMessage").value("Correcto, sigamos"));

        verify(gameService).answerQuestion("game-1", new AnswerRequestDto(1));
    }

    @Test
    void answerQuestionReturnsBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/game/game-1/answer").contentType("application/json"))
                .andExpect(status().isBadRequest());

        verify(gameService, never()).answerQuestion(any(), any());
    }

    @Test
    void getResultReturnsFinishedGameResult() throws Exception {
        GameResultDto response = new GameResultDto(
                "game-1",
                "player-1",
                7,
                10,
                true,
                2500,
                "Contratado"
        );
        when(gameService.getResult("game-1")).thenReturn(response);

        mockMvc.perform(get("/game/game-1/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("game-1"))
                .andExpect(jsonPath("$.playerId").value("player-1"))
                .andExpect(jsonPath("$.score").value(7))
                .andExpect(jsonPath("$.totalQuestions").value(10))
                .andExpect(jsonPath("$.passed").value(true))
                .andExpect(jsonPath("$.reward").value(2500))
                .andExpect(jsonPath("$.finalMessage").value("Contratado"));

        verify(gameService).getResult("game-1");
    }

    @Test
    void createGameWithMistakesReturnsCreatedGame() throws Exception {
        GameDtoResponse response = new GameDtoResponse(
                "mistakes-game-1",
                "player-1",
                null,
                GameStatus.CREATED,
                null
        );
        when(gameService.createGameWithMistakes(authenticatedUser)).thenReturn(response);

        mockMvc.perform(post("/game/mistakes").principal(authenticatedUser))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("mistakes-game-1"))
                .andExpect(jsonPath("$.playerId").value("player-1"))
                .andExpect(jsonPath("$.difficulty").isEmpty())
                .andExpect(jsonPath("$.status").value("CREATED"));

        verify(gameService).createGameWithMistakes(authenticatedUser);
    }

    @Test
    void nextQuestionReturnsNotFoundWhenGameDoesNotExist() throws Exception {
        when(gameService.nextQuestion("missing-game"))
                .thenThrow(new GameNotFoundException("Game not found"));

        mockMvc.perform(get("/game/missing-game/nextQuestion"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Game not found"));
    }
}
