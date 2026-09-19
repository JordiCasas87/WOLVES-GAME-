package com.jordi.wolves.wolves_api.security.jwt;

import com.jordi.wolves.wolves_api.player.enums.Role;
import com.jordi.wolves.wolves_api.player.model.Player;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String TEST_SECRET =
            "VGhpc0lzQVRlc3RPbmx5U2VjcmV0S2V5VGhhdElzTG9uZ0Vub3VnaEZvckhTMjU2";

    private JwtService jwtService;
    private Player player;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET);
        player = new Player("jordi", "encoded-password", Role.USER, 30);
    }

    @Test
    void generateTokenStoresUsernameAsSubject() {
        String token = jwtService.generateToken(player);

        String username = jwtService.extractUsername(token);

        assertEquals("jordi", username);
    }

    @Test
    void generateTokenStoresAdditionalClaims() {
        String token = jwtService.generateToken(Map.of("scope", "game:play"), player);

        String scope = jwtService.extractClaim(token, claims -> claims.get("scope", String.class));

        assertEquals("game:play", scope);
    }

    @Test
    void isTokenValidReturnsTrueForMatchingUser() {
        String token = jwtService.generateToken(player);

        boolean result = jwtService.isTokenValid(token, player);

        assertTrue(result);
    }

    @Test
    void isTokenValidReturnsFalseForDifferentUser() {
        String token = jwtService.generateToken(player);
        Player otherPlayer = new Player("marcus", "encoded-password", Role.USER, 28);

        boolean result = jwtService.isTokenValid(token, otherPlayer);

        assertFalse(result);
    }

    @Test
    void extractUsernameRejectsTamperedToken() {
        String token = jwtService.generateToken(player);
        String[] segments = token.split("\\.");
        String signature = segments[2];
        String tamperedSignature = (signature.startsWith("a") ? "b" : "a")
                + signature.substring(1);
        String tamperedToken = segments[0] + "." + segments[1] + "." + tamperedSignature;

        assertThrows(JwtException.class, () -> jwtService.extractUsername(tamperedToken));
    }
}
