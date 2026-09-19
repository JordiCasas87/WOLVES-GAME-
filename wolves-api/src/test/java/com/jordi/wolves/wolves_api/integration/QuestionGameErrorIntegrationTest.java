package com.jordi.wolves.wolves_api.integration;

import com.jordi.wolves.wolves_api.game.model.Game;
import com.jordi.wolves.wolves_api.game.repository.GameRepository;
import com.jordi.wolves.wolves_api.player.repository.PlayerRepository;
import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import com.jordi.wolves.wolves_api.question.model.Question;
import com.jordi.wolves.wolves_api.question.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration")
@AutoConfigureMockMvc(addFilters = false)
class QuestionGameErrorIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PlayerRepository playerRepository;
    @MockBean private QuestionRepository questionRepository;
    @MockBean private GameRepository gameRepository;

    @Test
    void deletingMissingQuestionReturnsNotFoundWithoutDeletingAnything() throws Exception {
        when(questionRepository.existsById("missing-question")).thenReturn(false);

        mockMvc.perform(delete("/questions/missing-question"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Question not found with id: missing-question"));

        verify(questionRepository).existsById("missing-question");
    }

    @Test
    void requestingQuestionFromMissingGameReturnsNotFound() throws Exception {
        when(gameRepository.findById("missing-game")).thenReturn(Optional.empty());

        mockMvc.perform(get("/game/missing-game/nextQuestion"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Game Not Found!"));
    }

    @Test
    void answeringBeforeRequestingAQuestionReturnsBadRequest() throws Exception {
        Question question = new Question(
                "question-1", "Introducción", "Pregunta", List.of("A", "B"), 0, Difficulty.EASY);
        Game game = new Game("player-1", Difficulty.EASY, List.of(question), 1_500);
        game.setId("game-1");
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

        mockMvc.perform(post("/game/game-1/answer")
                        .contentType("application/json")
                        .content("{\"selectedAnswer\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No question to answer right now"));
    }
}
