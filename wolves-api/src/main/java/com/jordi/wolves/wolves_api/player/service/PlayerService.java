package com.jordi.wolves.wolves_api.player.service;

import com.jordi.wolves.wolves_api.player.dto.PlayerDtoRequest;
import com.jordi.wolves.wolves_api.player.dto.PlayerDtoResponse;
import com.jordi.wolves.wolves_api.player.exception.PlayerNotFoundException;
import com.jordi.wolves.wolves_api.player.mapper.PlayerMapper;
import com.jordi.wolves.wolves_api.player.model.Player;
import com.jordi.wolves.wolves_api.player.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class PlayerService {

    PlayerRepository playerRepo;
    PlayerMapper playerMapper;

    public PlayerService(PlayerRepository playerRepo, PlayerMapper playerMapper) {
        this.playerRepo = playerRepo;
        this.playerMapper = playerMapper;
    }

    public PlayerDtoResponse createPlayer(PlayerDtoRequest playerDtoRequest) {

        Player newPlayer = playerMapper.toEntity(playerDtoRequest);
        Player saved = playerRepo.save(newPlayer);
        return playerMapper.toDto(saved);

    }

    public PlayerDtoResponse getPlayerById ( String id){

        Optional <Player> findedPlayer = playerRepo.findById(id);
        if (findedPlayer.isEmpty()){
            throw new PlayerNotFoundException("Player not found");
        }
        Player findedPlayerObject = findedPlayer.get();

       return playerMapper.toDto(findedPlayerObject);

    }

    public List<PlayerDtoResponse> getAllPlayers (){
       List<Player> allPlayers = playerRepo.findAll();

       List<PlayerDtoResponse> allDtoPlayers = allPlayers.stream()
               .map(playerMapper::toDto)
               .toList();

       return allDtoPlayers;

    }

    public void deletePlayerById ( String id){
        Optional <Player> findedPlayer = playerRepo.findById(id);
        if (findedPlayer.isEmpty()){
            throw new PlayerNotFoundException("Player not found");
        }

        Player findedPlayerObject = findedPlayer.get();
        playerRepo.delete(findedPlayerObject);

    }

    //metodos de player


    public Player loadPlayer(String playerId) {
        return playerRepo.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));
    }

    public void registerIncorrectQuestion(Player player, String questionId) {
        player.getIncorrectQuestionsIdList().add(questionId);
        playerRepo.save(player);
    }

    public void applyGameResult(Player player, int reward, boolean passed) {
        player.incrementGamesPlayed();

        if (passed) {
            player.incrementLevel();
            player.incrementMoney(reward);
        }

        playerRepo.save(player);
    }

    // Se usará cuando exista autenticación JWT
// public Player getPlayerByUsername(String username) {
//     return playerRepo.findByUsername(username)
//             .orElseThrow(() -> new PlayerNotFoundException(username));
// }

}
