package com.jordi.wolves.wolves_api.player.service;

import com.jordi.wolves.wolves_api.player.dto.PlayerAdminUpdateDto;
import com.jordi.wolves.wolves_api.player.dto.PlayerDtoRequest;
import com.jordi.wolves.wolves_api.player.dto.PlayerDtoResponse;
import com.jordi.wolves.wolves_api.player.dto.PlayerMeDto;
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
import org.springframework.security.core.Authentication;

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

    @Mock
    private Authentication authentication;

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

    @Test
    void loadPlayerReturnsPlayerWhenItExists() {
        Player player = player(PLAYER_ID, PLAYER_NAME, 30);
        when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(player));

        Player result = playerService.loadPlayer(PLAYER_ID);

        assertSame(player, result);
    }

    @Test
    void loadPlayerThrowsWhenPlayerDoesNotExist() {
        when(playerRepository.findById("missing-player")).thenReturn(Optional.empty());

        PlayerNotFoundException exception = assertThrows(
                PlayerNotFoundException.class,
                () -> playerService.loadPlayer("missing-player")
        );

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    void loadPlayerByNameReturnsPlayerWhenItExists() {
        Player player = player(PLAYER_ID, PLAYER_NAME, 30);
        when(playerRepository.findByName(PLAYER_NAME)).thenReturn(Optional.of(player));

        Player result = playerService.loadPlayerByName(PLAYER_NAME);

        assertSame(player, result);
    }

    @Test
    void loadPlayerByNameThrowsWhenPlayerDoesNotExist() {
        when(playerRepository.findByName("missing-name")).thenReturn(Optional.empty());

        PlayerNotFoundException exception = assertThrows(
                PlayerNotFoundException.class,
                () -> playerService.loadPlayerByName("missing-name")
        );

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    void registerIncorrectQuestionAddsQuestionAndSavesPlayer() {
        Player player = player(PLAYER_ID, PLAYER_NAME, 30);

        playerService.registerIncorrectQuestion(player, "question-1");

        assertEquals(List.of("question-1"), player.getIncorrectQuestionsIdList());
        verify(playerRepository).save(player);
    }

    @Test
    void applyGameResultIncrementsStatisticsAndRewardWhenPlayerPasses() {
        Player player = player(PLAYER_ID, PLAYER_NAME, 30);
        player.setGamesPlayed(2);
        player.setLevel(3);
        player.setMoney(2500);

        playerService.applyGameResult(player, 1500, true);

        assertEquals(3, player.getGamesPlayed());
        assertEquals(4, player.getLevel());
        assertEquals(4000, player.getMoney());
        verify(playerRepository).save(player);
    }

    @Test
    void applyGameResultOnlyIncrementsGamesPlayedWhenPlayerFails() {
        Player player = player(PLAYER_ID, PLAYER_NAME, 30);
        player.setGamesPlayed(2);
        player.setLevel(3);
        player.setMoney(2500);

        playerService.applyGameResult(player, 1500, false);

        assertEquals(3, player.getGamesPlayed());
        assertEquals(3, player.getLevel());
        assertEquals(2500, player.getMoney());
        verify(playerRepository).save(player);
    }

    @Test
    void getMeLoadsAndMapsAuthenticatedPlayer() {
        Player player = player(PLAYER_ID, PLAYER_NAME, 30);
        PlayerMeDto expectedResponse = meResponseFor(player);
        when(authentication.getName()).thenReturn(PLAYER_NAME);
        when(playerRepository.findByName(PLAYER_NAME)).thenReturn(Optional.of(player));
        when(playerMapper.toMeDto(player)).thenReturn(expectedResponse);

        PlayerMeDto result = playerService.getMe(authentication);

        assertSame(expectedResponse, result);
        verify(playerRepository).findByName(PLAYER_NAME);
        verify(playerMapper).toMeDto(player);
    }

    @Test
    void updateByAdminUpdatesEveryProvidedField() {
        Player player = player(PLAYER_ID, PLAYER_NAME, 30);
        PlayerAdminUpdateDto request = new PlayerAdminUpdateDto(
                "updated-name",
                35,
                4,
                Role.ADMIN,
                5000
        );
        PlayerDtoResponse expectedResponse = new PlayerDtoResponse(
                PLAYER_ID,
                "updated-name",
                35,
                player.getDateOfCreation(),
                4,
                5000,
                player.getIncorrectQuestionsIdList(),
                Role.ADMIN
        );
        when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(player));
        when(playerRepository.save(player)).thenReturn(player);
        when(playerMapper.toDto(player)).thenReturn(expectedResponse);

        PlayerDtoResponse result = playerService.updateByAdmin(PLAYER_ID, request);

        assertSame(expectedResponse, result);
        assertEquals("updated-name", player.getName());
        assertEquals(35, player.getAge());
        assertEquals(4, player.getLevel());
        assertEquals(5000, player.getMoney());
        assertEquals(Role.ADMIN, player.getRole());
        verify(playerRepository).save(player);
    }

    @Test
    void updateByAdminPreservesFieldsThatAreNotProvided() {
        Player player = player(PLAYER_ID, PLAYER_NAME, 30);
        player.setLevel(2);
        player.setMoney(1500);
        PlayerAdminUpdateDto request = new PlayerAdminUpdateDto(
                null,
                31,
                null,
                null,
                null
        );
        PlayerDtoResponse expectedResponse = new PlayerDtoResponse(
                PLAYER_ID,
                PLAYER_NAME,
                31,
                player.getDateOfCreation(),
                2,
                1500,
                player.getIncorrectQuestionsIdList(),
                Role.USER
        );
        when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(player));
        when(playerRepository.save(player)).thenReturn(player);
        when(playerMapper.toDto(player)).thenReturn(expectedResponse);

        PlayerDtoResponse result = playerService.updateByAdmin(PLAYER_ID, request);

        assertSame(expectedResponse, result);
        assertEquals(PLAYER_NAME, player.getName());
        assertEquals(31, player.getAge());
        assertEquals(2, player.getLevel());
        assertEquals(1500, player.getMoney());
        assertEquals(Role.USER, player.getRole());
        verify(playerRepository).save(player);
    }

    @Test
    void updateByAdminThrowsWhenPlayerDoesNotExist() {
        PlayerAdminUpdateDto request = new PlayerAdminUpdateDto(
                "updated-name",
                null,
                null,
                null,
                null
        );
        when(playerRepository.findById("missing-player")).thenReturn(Optional.empty());

        PlayerNotFoundException exception = assertThrows(
                PlayerNotFoundException.class,
                () -> playerService.updateByAdmin("missing-player", request)
        );

        assertEquals("Player not found", exception.getMessage());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void updateMyNotesUpdatesSavesAndMapsAuthenticatedPlayer() {
        Player player = player(PLAYER_ID, PLAYER_NAME, 30);
        PlayerMeDto expectedResponse = new PlayerMeDto(
                PLAYER_ID,
                PLAYER_NAME,
                Role.USER.name(),
                0,
                0,
                0,
                "Repasar Spring Security"
        );
        when(authentication.getName()).thenReturn(PLAYER_NAME);
        when(playerRepository.findByName(PLAYER_NAME)).thenReturn(Optional.of(player));
        when(playerMapper.toMeDto(player)).thenReturn(expectedResponse);

        PlayerMeDto result = playerService.updateMyNotes(
                authentication,
                "Repasar Spring Security"
        );

        assertSame(expectedResponse, result);
        assertEquals("Repasar Spring Security", player.getNotes());
        verify(playerRepository).save(player);
        verify(playerMapper).toMeDto(player);
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

    private PlayerMeDto meResponseFor(Player player) {
        return new PlayerMeDto(
                player.getId(),
                player.getName(),
                player.getRole().name(),
                player.getMoney(),
                player.getGamesPlayed(),
                player.getLevel(),
                player.getNotes()
        );
    }
}
