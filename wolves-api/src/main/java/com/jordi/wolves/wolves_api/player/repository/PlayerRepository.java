package com.jordi.wolves.wolves_api.player.repository;

import com.jordi.wolves.wolves_api.player.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerRepository extends MongoRepository<Player,String> {

}
