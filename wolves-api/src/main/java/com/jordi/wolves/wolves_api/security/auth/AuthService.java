package com.jordi.wolves.wolves_api.security.auth;


import com.jordi.wolves.wolves_api.player.dto.PlayerDtoRequest;
import com.jordi.wolves.wolves_api.player.mapper.PlayerMapper;
import com.jordi.wolves.wolves_api.player.model.Player;
import com.jordi.wolves.wolves_api.player.repository.PlayerRepository;
import com.jordi.wolves.wolves_api.security.auth.dtos.AuthResponse;
import com.jordi.wolves.wolves_api.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PlayerRepository repository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final PlayerMapper playerMapper;

    public AuthService(PlayerRepository repository, JwtService jwtService,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       PlayerMapper playerMapper) {
        this.repository = repository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.playerMapper = playerMapper;
    }

    public AuthResponse register(PlayerDtoRequest request) {

        if (repository.findByName(request.name()).isPresent()) {
            throw new IllegalStateException("Username already exists");
        }

        Player player = playerMapper.toEntity(request);
        player.setPassword(passwordEncoder.encode(request.password()));
        repository.save(player);
        var jwtToken = jwtService.generateToken(player);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse login(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        var player = repository.findByName(username).orElseThrow();
        var jwtToken = jwtService.generateToken(player);
        return new AuthResponse(jwtToken);
    }

}
