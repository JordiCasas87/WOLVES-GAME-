package com.jordi.wolves.wolves_api.player.mapper;

import com.jordi.wolves.wolves_api.player.dto.PlayerDtoRequest;
import com.jordi.wolves.wolves_api.player.dto.PlayerDtoResponse;
import com.jordi.wolves.wolves_api.player.dto.PlayerMeDto;
import com.jordi.wolves.wolves_api.player.dto.PlayerRankingDto;
import com.jordi.wolves.wolves_api.player.enums.Role;
import com.jordi.wolves.wolves_api.player.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerMapperTest {

    private PlayerMapper playerMapper;

    @BeforeEach
    void setUp() {
        playerMapper = new PlayerMapper();
    }

    @Test
    void toEntityMapsRequestAndAssignsUserRole() {
        PlayerDtoRequest request = new PlayerDtoRequest("jordi", "secret1", 30);

        Player result = playerMapper.toEntity(request);

        assertEquals("jordi", result.getName());
        assertEquals("secret1", result.getPassword());
        assertEquals(30, result.getAge());
        assertEquals(Role.USER, result.getRole());
        assertEquals(0, result.getLevel());
        assertEquals(0, result.getMoney());
        assertEquals(List.of(), result.getIncorrectQuestionsIdList());
    }

    @Test
    void toDtoMapsPublicPlayerData() {
        Player player = populatedPlayer();

        PlayerDtoResponse result = playerMapper.toDto(player);

        assertEquals("player-1", result.id());
        assertEquals("jordi", result.name());
        assertEquals(30, result.age());
        assertEquals(player.getDateOfCreation(), result.dateOfCreation());
        assertEquals(3, result.level());
        assertEquals(2500, result.money());
        assertEquals(List.of("question-1"), result.incorrectQuestionsIdList());
        assertEquals(Role.ADMIN, result.role());
    }

    @Test
    void toMeDtoMapsAuthenticatedPlayerProfile() {
        Player player = populatedPlayer();

        PlayerMeDto result = playerMapper.toMeDto(player);

        assertEquals("player-1", result.id());
        assertEquals("jordi", result.name());
        assertEquals("ADMIN", result.role());
        assertEquals(2500, result.money());
        assertEquals(4, result.gamesPlayed());
        assertEquals(3, result.level());
        assertEquals("Repasar testing", result.notes());
    }

    @Test
    void toRankingDtoMapsRankingFields() {
        Player player = populatedPlayer();

        PlayerRankingDto result = playerMapper.toRankingDto(player);

        assertEquals("player-1", result.playerId());
        assertEquals("jordi", result.name());
        assertEquals(2500, result.money());
    }

    private Player populatedPlayer() {
        Player player = new Player("jordi", "encoded-password", Role.ADMIN, 30);
        player.setId("player-1");
        player.setLevel(3);
        player.setGamesPlayed(4);
        player.setMoney(2500);
        player.setIncorrectQuestionsIdList(List.of("question-1"));
        player.setNotes("Repasar testing");
        return player;
    }
}
