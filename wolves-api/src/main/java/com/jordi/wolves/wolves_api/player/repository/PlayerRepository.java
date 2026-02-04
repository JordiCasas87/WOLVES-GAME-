package com.jordi.wolves.wolves_api.player.repository;

import com.jordi.wolves.wolves_api.player.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends MongoRepository<Player,String> {
    List<Player> findAllByOrderByMoneyDesc();

    //para el jwt
    Optional<Player> findByName(String name);

}
