package com.jordi.wolves.wolves_api.player.service;

import com.jordi.wolves.wolves_api.player.dto.PlayerDtoRequest;
import com.jordi.wolves.wolves_api.player.dto.PlayerDtoResponse;
import com.jordi.wolves.wolves_api.player.dto.PlayerRankingDto;
import com.jordi.wolves.wolves_api.player.enums.Role;
import com.jordi.wolves.wolves_api.player.exception.PlayerNotFoundException;
import com.jordi.wolves.wolves_api.player.mapper.PlayerMapper;
import com.jordi.wolves.wolves_api.player.model.Player;
import com.jordi.wolves.wolves_api.player.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    private static final String PLAYER_ID = "player-1";
    private static final String PLAYER_NAME = "jordi";

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerMapper playerMapper;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService(playerRepository, playerMapper);
    }

    @Test
    void createPlayerMapsSavesAndReturnsPlayer() {
        PlayerDtoRequest request = new PlayerDtoRequest(PLAYER_NAME, "secret1", 30);
        Player mappedPlayer = player(PLAYER_ID, PLAYER_NAME, 30);
        Player savedPlayer = player(PLAYER_ID, PLAYER_NAME, 30);
        PlayerDtoResponse expectedResponse = responseFor(savedPlayer);
        when(playerMapper.toEntity(request)).thenReturn(mappedPlayer);
        when(playerRepository.save(mappedPlayer)).thenReturn(savedPlayer);
        when(playerMapper.toDto(savedPlayer)).thenReturn(expectedResponse);

        PlayerDtoResponse result = playerService.createPlayer(request);

        assertSame(expectedResponse, result);
        verify(playerMapper).toEntity(request);
        verify(playerRepository).save(mappedPlayer);
        verify(playerMapper).toDto(savedPlayer);
    }

    @Test
    void getPlayerByIdReturnsMappedPlayerWhenItExists() {
        Player player = player(PLAYER_ID, PLAYER_NAME, 30);
        PlayerDtoResponse expectedResponse = responseFor(player);
        when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(player));
        when(playerMapper.toDto(player)).thenReturn(expectedResponse);

        PlayerDtoResponse result = playerService.getPlayerById(PLAYER_ID);

        assertSame(expectedResponse, result);
        verify(playerMapper).toDto(player);
    }

    @Test
    void getPlayerByIdThrowsWhenPlayerDoesNotExist() {
        when(playerRepository.findById("missing-player")).thenReturn(Optional.empty());

        PlayerNotFoundException exception = assertThrows(
                PlayerNotFoundException.class,
                () -> playerService.getPlayerById("missing-player")
        );

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    void getAllPlayersMapsEveryPlayer() {
        Player firstPlayer = player("player-1", "jordi", 30);
        Player secondPlayer = player("player-2", "marcus", 28);
        PlayerDtoResponse firstResponse = responseFor(firstPlayer);
        PlayerDtoResponse secondResponse = responseFor(secondPlayer);
        when(playerRepository.findAll()).thenReturn(List.of(firstPlayer, secondPlayer));
        when(playerMapper.toDto(firstPlayer)).thenReturn(firstResponse);
        when(playerMapper.toDto(secondPlayer)).thenReturn(secondResponse);

        List<PlayerDtoResponse> result = playerService.getAllPlayers();

        assertEquals(List.of(firstResponse, secondResponse), result);
        verify(playerMapper).toDto(firstPlayer);
        verify(playerMapper).toDto(secondPlayer);
    }

    @Test
    void deletePlayerByIdDeletesPlayerWhenItExists() {
        Player player = player(PLAYER_ID, PLAYER_NAME, 30);
        when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(player));

        playerService.deletePlayerById(PLAYER_ID);

        verify(playerRepository).delete(player);
    }

    @Test
    void deletePlayerByIdThrowsWhenPlayerDoesNotExist() {
        when(playerRepository.findById("missing-player")).thenReturn(Optional.empty());

        PlayerNotFoundException exception = assertThrows(
                PlayerNotFoundException.class,
                () -> playerService.deletePlayerById("missing-player")
        );

        assertEquals("Player not found", exception.getMessage());
        verify(playerRepository, never()).delete(any(Player.class));
    }

    @Test
    void getRankingMapsRepositoryRankingInTheSameOrder() {
        Player richestPlayer = player("player-1", "rich", 30);
        richestPlayer.setMoney(5000);
        Player secondPlayer = player("player-2", "second", 28);
        secondPlayer.setMoney(2500);
        PlayerRankingDto richestResponse = new PlayerRankingDto("player-1", "rich", 5000);
        PlayerRankingDto secondResponse = new PlayerRankingDto("player-2", "second", 2500);
        when(playerRepository.findAllByOrderByMoneyDesc())
                .thenReturn(List.of(richestPlayer, secondPlayer));
        when(playerMapper.toRankingDto(richestPlayer)).thenReturn(richestResponse);
        when(playerMapper.toRankingDto(secondPlayer)).thenReturn(secondResponse);

        List<PlayerRankingDto> result = playerService.getRanking();

        assertEquals(List.of(richestResponse, secondResponse), result);
        verify(playerMapper).toRankingDto(richestPlayer);
        verify(playerMapper).toRankingDto(secondPlayer);
    }

    private Player player(String id, String name, int age) {
        Player player = new Player(name, "encoded-password", Role.USER, age);
        player.setId(id);
        return player;
    }

    private PlayerDtoResponse responseFor(Player player) {
        return new PlayerDtoResponse(
                player.getId(),
                player.getName(),
                player.getAge(),
                LocalDate.now(),
                player.getLevel(),
                player.getMoney(),
                player.getIncorrectQuestionsIdList(),
                player.getRole()
        );
    }
}
