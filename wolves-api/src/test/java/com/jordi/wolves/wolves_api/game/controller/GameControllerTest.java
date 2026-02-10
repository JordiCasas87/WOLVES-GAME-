package com.jordi.wolves.wolves_api.game.controller;

import com.jordi.wolves.wolves_api.game.dto.AnswerResponseDto;
import com.jordi.wolves.wolves_api.game.dto.GameDtoResponse;
import com.jordi.wolves.wolves_api.game.dto.GameResultDto;
import com.jordi.wolves.wolves_api.game.enums.GameStatus;
import com.jordi.wolves.wolves_api.game.service.GameService;
import com.jordi.wolves.wolves_api.question.dto.QuestionDtoNextResponse;
import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import com.jordi.wolves.wolves_api.security.jwt.JwtAuthenticationFilter;
import com.jordi.wolves.wolves_api.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;


@WebMvcTest(GameController.class)
class GameControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "jordi", roles = {"USER"})
    void createGame() throws Exception {

        GameDtoResponse mockResponse = new GameDtoResponse(
                "gameId123",
                "playerId123",
                Difficulty.EASY,
                GameStatus.CREATED,
                null
        );

        when(gameService.createGame(any(), eq(Difficulty.EASY)))
                .thenReturn(mockResponse);


        mockMvc.perform(
                        post("/game/new")
                                .param("difficulty", "EASY")
                                .with(csrf())
                )
                .andExpect(status().isOk());

    }


    @Test
    @WithMockUser(username = "jordi", roles = {"USER"})
    void nextQuestion() throws Exception {

        QuestionDtoNextResponse mockResponse =
                new QuestionDtoNextResponse(
                        1,
                        "Intro de la pregunta",
                        "Texto de la pregunta",
                        List.of(
                                "Respuesta 1",
                                "Respuesta 2",
                                "Respuesta 3",
                                "Respuesta 4"
                        )
                );

        when(gameService.nextQuestion("gameId123"))
                .thenReturn(mockResponse);

        mockMvc.perform(
                        get("/game/gameId123/nextQuestion")
                )
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(username = "jordi", roles = {"USER"})
    void answerQuestion() throws Exception {

        AnswerResponseDto mockResponse =
                new AnswerResponseDto(
                        true,
                        "Correcto, sigamos…"
                );

        when(gameService.answerQuestion(eq("gameId123"), any()))
                .thenReturn(mockResponse);

        String requestBody = """
            {
              "selectedAnswer": 1
            }
            """;

        mockMvc.perform(
                        post("/game/gameId123/answer")
                                .with(csrf())
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(username = "jordi", roles = {"USER"})
    void getResult() throws Exception {

        GameResultDto mockResponse =
                new GameResultDto(
                        "gameId123",
                        "playerId123",
                        7,
                        10,
                        true,
                        2500,
                        "Contratado, no te comeré… por ahora."
                );

        when(gameService.getResult("gameId123"))
                .thenReturn(mockResponse);

        mockMvc.perform(
                        get("/game/gameId123/result")
                )
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(username = "jordi", roles = {"USER"})
    void createGameWithMistakes() throws Exception {

        GameDtoResponse mockResponse = new GameDtoResponse(
                "gameIdMistakes",
                "playerId123",
                null,
                GameStatus.CREATED,
                "¿Volvemos a repasar tus errores?"
        );

        when(gameService.createGameWithMistakes(any()))
                .thenReturn(mockResponse);

        mockMvc.perform(
                        post("/game/mistakes")
                                .with(csrf())
                )
                .andExpect(status().isOk());
    }
}