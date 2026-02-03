package com.jordi.wolves.wolves_api.game.repository;

import com.jordi.wolves.wolves_api.game.enums.GameStatus;
import com.jordi.wolves.wolves_api.game.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends MongoRepository<Game,String> {

    Optional<Game> findFirstByPlayerIdAndStatusIn(String playerId, List<GameStatus> statusList);
}
