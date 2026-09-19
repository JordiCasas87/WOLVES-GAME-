package com.jordi.wolves.wolves_api.integration;

import com.jordi.wolves.wolves_api.game.model.Game;
import com.jordi.wolves.wolves_api.game.repository.GameRepository;
import com.jordi.wolves.wolves_api.player.enums.Role;
import com.jordi.wolves.wolves_api.player.model.Player;
import com.jordi.wolves.wolves_api.player.repository.PlayerRepository;
import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import com.jordi.wolves.wolves_api.question.model.Question;
import com.jordi.wolves.wolves_api.question.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration")
@AutoConfigureMockMvc(addFilters = false)
class ApplicationFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PlayerRepository playerRepository;
    @MockBean private QuestionRepository questionRepository;
    @MockBean private GameRepository gameRepository;

    @Test
    void registrationFlowsThroughControllerServiceMapperAndRepository() throws Exception {
        when(playerRepository.findByName("new-user")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player player = invocation.getArgument(0);
            player.setId("player-1");
            return player;
        });

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"name":"new-user","password":"secret1","age":30}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void playerCreationFlowsThroughControllerServiceMapperAndRepository() throws Exception {
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player player = invocation.getArgument(0);
            player.setId("player-1");
            return player;
        });

        mockMvc.perform(post("/players")
                        .contentType("application/json")
                        .content("""
                                {"name":"jordi","password":"secret1","age":30}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("player-1"))
                .andExpect(jsonPath("$.name").value("jordi"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void questionListingFlowsThroughControllerServiceAndRepository() throws Exception {
        when(questionRepository.findAll()).thenReturn(List.of(
                question("question-1", "Primera pregunta"),
                question("question-2", "Segunda pregunta")
        ));

        mockMvc.perform(get("/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("question-1"))
                .andExpect(jsonPath("$[1].text").value("Segunda pregunta"));
    }

    @Test
    void completeGameFlowsAcrossWebServiceAndRepositoryLayers() throws Exception {
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                "jordi", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        Player player = new Player("jordi", "encoded-password", Role.USER, 30);
        player.setId("player-1");
        Question question = question("question-1", "Primera pregunta");
        AtomicReference<Game> storedGame = new AtomicReference<>();

        when(playerRepository.findByName("jordi")).thenReturn(Optional.of(player));
        when(playerRepository.findById("player-1")).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameRepository.findFirstByPlayerIdAndStatusIn(any(), any())).thenReturn(Optional.empty());
        when(questionRepository.findByDifficulty(Difficulty.EASY)).thenReturn(List.of(question));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game game = invocation.getArgument(0);
            if (game.getId() == null) game.setId("game-1");
            storedGame.set(game);
            return game;
        });
        when(gameRepository.findById("game-1"))
                .thenAnswer(invocation -> Optional.ofNullable(storedGame.get()));

        mockMvc.perform(post("/game/new")
                        .principal(authentication)
                        .param("difficulty", "EASY"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("game-1"));

        mockMvc.perform(get("/game/game-1/nextQuestion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberQuestion").value(1));

        mockMvc.perform(post("/game/game-1/answer")
                        .contentType("application/json")
                        .content("{\"selectedAnswer\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true));

        mockMvc.perform(get("/game/game-1/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(1))
                .andExpect(jsonPath("$.totalQuestions").value(1))
                .andExpect(jsonPath("$.passed").value(false));
    }

    private Question question(String id, String text) {
        return new Question(id, "Introducción", text, List.of("A", "B"), 0, Difficulty.EASY);
    }
}
