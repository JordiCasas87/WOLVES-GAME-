package com.jordi.wolves.wolves_api.security.auth;

import com.jordi.wolves.wolves_api.player.dto.PlayerDtoRequest;
import com.jordi.wolves.wolves_api.player.enums.Role;
import com.jordi.wolves.wolves_api.player.mapper.PlayerMapper;
import com.jordi.wolves.wolves_api.player.model.Player;
import com.jordi.wolves.wolves_api.player.repository.PlayerRepository;
import com.jordi.wolves.wolves_api.security.auth.dtos.AuthResponse;
import com.jordi.wolves.wolves_api.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PlayerMapper playerMapper;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                playerRepository,
                jwtService,
                passwordEncoder,
                authenticationManager,
                playerMapper
        );
    }

    @Test
    void registerEncodesPasswordSavesPlayerAndReturnsToken() {
        PlayerDtoRequest request = new PlayerDtoRequest("jordi", "secret1", 30);
        Player player = new Player("jordi", "secret1", Role.USER, 30);
        when(playerRepository.findByName("jordi")).thenReturn(Optional.empty());
        when(playerMapper.toEntity(request)).thenReturn(player);
        when(passwordEncoder.encode("secret1")).thenReturn("encoded-password");
        when(jwtService.generateToken(player)).thenReturn("jwt-token");

        AuthResponse result = authService.register(request);

        assertEquals("jwt-token", result.token());
        assertEquals("encoded-password", player.getPassword());
        verify(playerRepository).save(player);
        verify(jwtService).generateToken(player);
    }

    @Test
    void registerThrowsWhenUsernameAlreadyExists() {
        PlayerDtoRequest request = new PlayerDtoRequest("jordi", "secret1", 30);
        Player existingPlayer = new Player("jordi", "encoded-password", Role.USER, 30);
        when(playerRepository.findByName("jordi")).thenReturn(Optional.of(existingPlayer));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authService.register(request)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(playerMapper, never()).toEntity(any());
        verify(playerRepository, never()).save(any(Player.class));
        verify(jwtService, never()).generateToken(any(Player.class));
    }

    @Test
    void loginAuthenticatesCredentialsAndReturnsToken() {
        Player player = new Player("jordi", "encoded-password", Role.USER, 30);
        when(playerRepository.findByName("jordi")).thenReturn(Optional.of(player));
        when(jwtService.generateToken(player)).thenReturn("jwt-token");

        AuthResponse result = authService.login("jordi", "secret1");

        assertEquals("jwt-token", result.token());
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("jordi", "secret1")
        );
        verify(jwtService).generateToken(player);
    }

    @Test
    void loginThrowsWhenAuthenticatedPlayerCannotBeFound() {
        when(playerRepository.findByName("missing-user")).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> authService.login("missing-user", "secret1")
        );

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("missing-user", "secret1")
        );
        verify(jwtService, never()).generateToken(any(Player.class));
    }

    @Test
    void loginPropagatesInvalidCredentialsWithoutLoadingPlayer() {
        BadCredentialsException expectedException = new BadCredentialsException("Bad credentials");
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("jordi", "wrong-password")
        )).thenThrow(expectedException);

        BadCredentialsException result = assertThrows(
                BadCredentialsException.class,
                () -> authService.login("jordi", "wrong-password")
        );

        assertSame(expectedException, result);
        verify(playerRepository, never()).findByName(any());
        verify(jwtService, never()).generateToken(any(Player.class));
    }
}
