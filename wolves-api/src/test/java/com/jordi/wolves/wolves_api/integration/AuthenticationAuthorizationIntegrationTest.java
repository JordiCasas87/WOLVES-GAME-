package com.jordi.wolves.wolves_api.integration;

import com.jordi.wolves.wolves_api.game.repository.GameRepository;
import com.jordi.wolves.wolves_api.player.model.Player;
import com.jordi.wolves.wolves_api.player.repository.PlayerRepository;
import com.jordi.wolves.wolves_api.question.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration")
@AutoConfigureMockMvc
class AuthenticationAuthorizationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PlayerRepository playerRepository;
    @MockBean private QuestionRepository questionRepository;
    @MockBean private GameRepository gameRepository;

    @Test
    void duplicateRegistrationReturnsBadRequestAcrossApplicationLayers() throws Exception {
        when(playerRepository.findByName("existing-user"))
                .thenReturn(Optional.of(new Player()));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"name":"existing-user","password":"secret1","age":30}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void regularUserCannotReachAdministrativePlayerFlow() throws Exception {
        mockMvc.perform(get("/players"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(playerRepository);
    }
}
