package com.jordi.wolves.wolves_api.player.controller;

import com.jordi.wolves.wolves_api.player.dto.PlayerAdminUpdateDto;
import com.jordi.wolves.wolves_api.player.dto.PlayerDtoRequest;
import com.jordi.wolves.wolves_api.player.dto.PlayerDtoResponse;
import com.jordi.wolves.wolves_api.player.dto.PlayerMeDto;
import com.jordi.wolves.wolves_api.player.dto.PlayerRankingDto;
import com.jordi.wolves.wolves_api.player.enums.Role;
import com.jordi.wolves.wolves_api.player.exception.PlayerNotFoundException;
import com.jordi.wolves.wolves_api.player.service.PlayerService;
import com.jordi.wolves.wolves_api.security.jwt.JwtAuthenticationFilter;
import com.jordi.wolves.wolves_api.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlayerControllerTest {

    private final Authentication authenticatedUser = UsernamePasswordAuthenticationToken.authenticated(
            "jordi",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @Test
    void createPlayerReturnsCreatedPlayer() throws Exception {
        PlayerDtoRequest request = new PlayerDtoRequest("jordi", "secret1", 30);
        PlayerDtoResponse response = playerResponse("player-1", "jordi", 30);
        when(playerService.createPlayer(request)).thenReturn(response);

        mockMvc.perform(post("/players")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "jordi",
                                  "password": "secret1",
                                  "age": 30
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("player-1"))
                .andExpect(jsonPath("$.name").value("jordi"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(playerService).createPlayer(request);
    }

    @Test
    void createPlayerReturnsValidationErrorsForBlankNameAndShortPassword() throws Exception {
        mockMvc.perform(post("/players")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "",
                                  "password": "123",
                                  "age": 30
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name is required"))
                .andExpect(jsonPath("$.password")
                        .value("Password must be at least 6 characters long"));

        verify(playerService, never()).createPlayer(any());
    }

    @Test
    void createPlayerReturnsValidationErrorForNegativeAge() throws Exception {
        mockMvc.perform(post("/players")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "jordi",
                                  "password": "secret1",
                                  "age": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.age").value("Age must be positive"));

        verify(playerService, never()).createPlayer(any());
    }

    @Test
    void getPlayerByIdReturnsPlayer() throws Exception {
        PlayerDtoResponse response = playerResponse("player-1", "jordi", 30);
        when(playerService.getPlayerById("player-1")).thenReturn(response);

        mockMvc.perform(get("/players/player-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("player-1"))
                .andExpect(jsonPath("$.name").value("jordi"));

        verify(playerService).getPlayerById("player-1");
    }

    @Test
    void getPlayerByIdReturnsNotFoundWhenPlayerDoesNotExist() throws Exception {
        when(playerService.getPlayerById("missing-player"))
                .thenThrow(new PlayerNotFoundException("Player not found"));

        mockMvc.perform(get("/players/missing-player"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Player not found"));
    }

    @Test
    void getAllPlayersReturnsPlayerList() throws Exception {
        when(playerService.getAllPlayers()).thenReturn(List.of(
                playerResponse("player-1", "jordi", 30),
                playerResponse("player-2", "marcus", 28)
        ));

        mockMvc.perform(get("/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("player-1"))
                .andExpect(jsonPath("$[1].id").value("player-2"));

        verify(playerService).getAllPlayers();
    }

    @Test
    void deletePlayerByIdReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/players/player-1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(playerService).deletePlayerById("player-1");
    }

    @Test
    void getRankingReturnsOrderedRanking() throws Exception {
        when(playerService.getRanking()).thenReturn(List.of(
                new PlayerRankingDto("player-1", "jordi", 5000),
                new PlayerRankingDto("player-2", "marcus", 2500)
        ));

        mockMvc.perform(get("/players/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].playerId").value("player-1"))
                .andExpect(jsonPath("$[0].money").value(5000))
                .andExpect(jsonPath("$[1].playerId").value("player-2"));

        verify(playerService).getRanking();
    }

    @Test
    void getMeReturnsAuthenticatedPlayerProfile() throws Exception {
        PlayerMeDto response = playerMeResponse("Notas");
        when(playerService.getMe(authenticatedUser)).thenReturn(response);

        mockMvc.perform(get("/me").principal(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("player-1"))
                .andExpect(jsonPath("$.name").value("jordi"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.notes").value("Notas"));

        verify(playerService).getMe(authenticatedUser);
    }

    @Test
    void updatePlayerByAdminReturnsUpdatedPlayer() throws Exception {
        PlayerAdminUpdateDto request = new PlayerAdminUpdateDto(
                "updated-name",
                31,
                2,
                Role.ADMIN,
                5000
        );
        PlayerDtoResponse response = new PlayerDtoResponse(
                "player-1",
                "updated-name",
                31,
                LocalDate.of(2026, 1, 1),
                2,
                5000,
                List.of(),
                Role.ADMIN
        );
        when(playerService.updateByAdmin("player-1", request)).thenReturn(response);

        mockMvc.perform(put("/players/player-1")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "updated-name",
                                  "age": 31,
                                  "level": 2,
                                  "role": "ADMIN",
                                  "money": 5000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated-name"))
                .andExpect(jsonPath("$.age").value(31))
                .andExpect(jsonPath("$.level").value(2))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.money").value(5000));

        verify(playerService).updateByAdmin("player-1", request);
    }

    @Test
    void updateMyNotesReturnsUpdatedProfile() throws Exception {
        PlayerMeDto response = playerMeResponse("Repasar Spring Security");
        when(playerService.updateMyNotes(authenticatedUser, "Repasar Spring Security"))
                .thenReturn(response);

        mockMvc.perform(patch("/me/notes")
                        .principal(authenticatedUser)
                        .contentType("application/json")
                        .content("""
                                {
                                  "notes": "Repasar Spring Security"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Repasar Spring Security"));

        verify(playerService).updateMyNotes(authenticatedUser, "Repasar Spring Security");
    }

    private PlayerDtoResponse playerResponse(String id, String name, int age) {
        return new PlayerDtoResponse(
                id,
                name,
                age,
                LocalDate.of(2026, 1, 1),
                0,
                0,
                List.of(),
                Role.USER
        );
    }

    private PlayerMeDto playerMeResponse(String notes) {
        return new PlayerMeDto(
                "player-1",
                "jordi",
                "USER",
                2500,
                3,
                2,
                notes
        );
    }
}
