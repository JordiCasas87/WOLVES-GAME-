package com.jordi.wolves.wolves_api.security.auth;

import com.jordi.wolves.wolves_api.player.dto.PlayerDtoRequest;
import com.jordi.wolves.wolves_api.security.auth.dtos.AuthResponse;
import com.jordi.wolves.wolves_api.security.jwt.JwtAuthenticationFilter;
import com.jordi.wolves.wolves_api.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @Test
    void registerReturnsAuthenticationToken() throws Exception {
        PlayerDtoRequest request = new PlayerDtoRequest("jordi", "secret1", 30);
        when(authService.register(request)).thenReturn(new AuthResponse("register-token"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "jordi",
                                  "password": "secret1",
                                  "age": 30
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("register-token"));

        verify(authService).register(request);
    }

    @Test
    void loginReturnsAuthenticationToken() throws Exception {
        when(authService.login("jordi", "secret1")).thenReturn(new AuthResponse("login-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "jordi",
                                  "password": "secret1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("login-token"));

        verify(authService).login("jordi", "secret1");
    }

    @Test
    void loginReturnsBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType("application/json"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(), any());
    }

    @Test
    void registerReturnsBadRequestWhenUsernameAlreadyExists() throws Exception {
        PlayerDtoRequest request = new PlayerDtoRequest("jordi", "secret1", 30);
        when(authService.register(request))
                .thenThrow(new IllegalStateException("Username already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "jordi",
                                  "password": "secret1",
                                  "age": 30
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists"));
    }
}
