package com.jordi.wolves.wolves_api.game.mapper;


import com.jordi.wolves.wolves_api.game.dto.GameDtoResponse;
import com.jordi.wolves.wolves_api.game.model.Game;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {


    public GameDtoResponse toDto (Game game) {
        return new GameDtoResponse(
                game.getId(),
                game.getPlayerId(),
                game.getDifficulty(),
                game.getStatus()
        );
    }

}
