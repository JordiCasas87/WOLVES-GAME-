package com.jordi.wolves.wolves_api.integration;

import com.jordi.wolves.wolves_api.game.repository.GameRepository;
import com.jordi.wolves.wolves_api.player.enums.Role;
import com.jordi.wolves.wolves_api.player.model.Player;
import com.jordi.wolves.wolves_api.player.repository.PlayerRepository;
import com.jordi.wolves.wolves_api.question.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration")
@AutoConfigureMockMvc(addFilters = false)
class PlayerRankingIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PlayerRepository playerRepository;
    @MockBean private QuestionRepository questionRepository;
    @MockBean private GameRepository gameRepository;

    @Test
    void rankingFlowsThroughRepositoryServiceMapperAndController() throws Exception {
        Player leader = player("player-1", "leader", 8_000);
        Player runnerUp = player("player-2", "runner-up", 3_000);
        when(playerRepository.findAllByOrderByMoneyDesc())
                .thenReturn(List.of(leader, runnerUp));

        mockMvc.perform(get("/players/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].playerId").value("player-1"))
                .andExpect(jsonPath("$[0].money").value(8_000))
                .andExpect(jsonPath("$[1].name").value("runner-up"));
    }

    @Test
    void administrativeUpdatePersistsAndReturnsMappedPlayer() throws Exception {
        Player player = player("player-1", "old-name", 500);
        when(playerRepository.findById("player-1")).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/players/player-1")
                        .contentType("application/json")
                        .content("""
                                {"name":"updated-name","age":35,"level":4,"role":"ADMIN","money":9000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("player-1"))
                .andExpect(jsonPath("$.name").value("updated-name"))
                .andExpect(jsonPath("$.age").value(35))
                .andExpect(jsonPath("$.level").value(4))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.money").value(9_000));

        verify(playerRepository).save(player);
    }

    @Test
    void missingPlayerReturnsNotFoundFromTheCompleteWebFlow() throws Exception {
        when(playerRepository.findById("missing-player")).thenReturn(Optional.empty());

        mockMvc.perform(get("/players/missing-player"))
                .andExpect(status().isNotFound());
    }

    private Player player(String id, String name, int money) {
        Player player = new Player(name, "encoded-password", Role.USER, 30);
        player.setId(id);
        player.setMoney(money);
        return player;
    }
}
