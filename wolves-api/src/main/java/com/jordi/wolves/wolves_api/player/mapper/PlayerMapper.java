package com.jordi.wolves.wolves_api.player.mapper;

import com.jordi.wolves.wolves_api.player.dto.PlayerDtoRequest;
import com.jordi.wolves.wolves_api.player.dto.PlayerDtoResponse;
import com.jordi.wolves.wolves_api.player.dto.PlayerMeDto;
import com.jordi.wolves.wolves_api.player.dto.PlayerRankingDto;
import com.jordi.wolves.wolves_api.player.enums.Role;
import com.jordi.wolves.wolves_api.player.model.Player;
import org.springframework.stereotype.Component;
import com.jordi.wolves.wolves_api.player.enums.Role;

@Component
public class PlayerMapper {

    public PlayerMeDto toMeDto(Player player) {
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

    public Player toEntity(PlayerDtoRequest dto) {
        return new Player(
                dto.name(),
                dto.password(),
                Role.USER,
                dto.age()
        );
    }

    public PlayerDtoResponse toDto(Player player) {
        return new PlayerDtoResponse(
                player.getId(),
                player.getName(),
                player.getAge(),
                player.getDateOfCreation(),
                player.getLevel(),
                player.getMoney(),
                player.getIncorrectQuestionsIdList(),
                player.getRole()
        );
    }
    public PlayerRankingDto toRankingDto(Player player) {
        return new PlayerRankingDto(
                player.getId(),
                player.getName(),
                player.getMoney()
        );
    }


}