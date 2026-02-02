package com.jordi.wolves.wolves_api.game.repository;

import com.jordi.wolves.wolves_api.game.model.Game;
import com.jordi.wolves.wolves_api.player.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameRepository extends MongoRepository<Game,String> {
}
