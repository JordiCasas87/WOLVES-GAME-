package com.jordi.wolves.wolves_api.security.config;

import com.jordi.wolves.wolves_api.game.controller.GameController;
import com.jordi.wolves.wolves_api.game.service.GameService;
import com.jordi.wolves.wolves_api.player.controller.PlayerController;
import com.jordi.wolves.wolves_api.player.service.PlayerService;
import com.jordi.wolves.wolves_api.question.controller.QuestionController;
import com.jordi.wolves.wolves_api.question.service.QuestionService;
import com.jordi.wolves.wolves_api.security.auth.AuthController;
import com.jordi.wolves.wolves_api.security.auth.AuthService;
import com.jordi.wolves.wolves_api.security.auth.dtos.AuthResponse;
import com.jordi.wolves.wolves_api.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({GameController.class, PlayerController.class, QuestionController.class, AuthController.class})
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private GameService gameService;
    @MockBean private PlayerService playerService;
    @MockBean private QuestionService questionService;
    @MockBean private AuthService authService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private AuthenticationProvider authenticationProvider;

    @BeforeEach
    void letJwtFilterContinueChain() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    void authenticationEndpointsArePublic() throws Exception {
        when(authService.login("jordi", "secret1")).thenReturn(new AuthResponse("token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"name\":\"jordi\",\"password\":\"secret1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void gameEndpointsRejectAnonymousUsers() throws Exception {
        mockMvc.perform(get("/game/game-1/result"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void gameEndpointsAllowUsers() throws Exception {
        mockMvc.perform(get("/game/game-1/result"))
                .andExpect(status().isOk());
        verify(gameService).getResult("game-1");
    }

    @Test
    @WithMockUser(roles = "USER")
    void administrativePlayerListRejectsUsers() throws Exception {
        mockMvc.perform(get("/players"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administrativePlayerListAllowsAdmins() throws Exception {
        when(playerService.getAllPlayers()).thenReturn(List.of());
        mockMvc.perform(get("/players"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deletingQuestionsRejectsUsers() throws Exception {
        mockMvc.perform(delete("/questions/question-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletingQuestionsAllowsAdmins() throws Exception {
        mockMvc.perform(delete("/questions/question-1"))
                .andExpect(status().isNoContent());
        verify(questionService).deleteQuestionById("question-1");
    }

    @Test
    void rankingRejectsAnonymousUsers() throws Exception {
        mockMvc.perform(get("/players/ranking"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void rankingAllowsAuthenticatedUsers() throws Exception {
        when(playerService.getRanking()).thenReturn(List.of());
        mockMvc.perform(get("/players/ranking"))
                .andExpect(status().isOk());
    }
}
