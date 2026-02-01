package com.jordi.wolves.wolves_api.player.mapper;

import com.jordi.wolves.wolves_api.player.dto.PlayerDtoRequest;
import com.jordi.wolves.wolves_api.player.dto.PlayerDtoResponse;
import com.jordi.wolves.wolves_api.player.model.Player;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

    public Player toEntity(PlayerDtoRequest dto) {
        return new Player(null, dto.name(), dto.age());
    }

    public PlayerDtoResponse toDto(Player player) {
        return new PlayerDtoResponse(
                player.getId(),
                player.getName(),
                player.getAge(),
                player.getDateOfCreation(),
                player.getLevel(),
                player.getMoney(),
                player.getIncorrectQuestionsIdList()
        );
    }
}