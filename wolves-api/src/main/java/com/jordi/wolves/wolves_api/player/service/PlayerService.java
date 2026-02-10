package com.jordi.wolves.wolves_api.player.service;

import com.jordi.wolves.wolves_api.player.dto.*;
import com.jordi.wolves.wolves_api.player.exception.PlayerNotFoundException;
import com.jordi.wolves.wolves_api.player.mapper.PlayerMapper;
import com.jordi.wolves.wolves_api.player.model.Player;
import com.jordi.wolves.wolves_api.player.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;


@Service
public class PlayerService {

    private final PlayerRepository playerRepo;
    private final PlayerMapper playerMapper;

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

    public List<PlayerRankingDto> getRanking() {

        return playerRepo.findAllByOrderByMoneyDesc()
                .stream()
                .map(playerMapper::toRankingDto)
                .toList();
    }

    //metodos de player

    public Player loadPlayer(String playerId) {
        return playerRepo.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));
    }

    public Player loadPlayerByName(String username) {
        return playerRepo.findByName(username)
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
    public PlayerMeDto getMe(Authentication authentication) {
        String username = authentication.getName(); // viene del JWT
        Player player = loadPlayerByName(username);
        return playerMapper.toMeDto(player);
    }


    public PlayerDtoResponse updateByAdmin(String playerId, PlayerAdminUpdateDto dto) {

        Player player = playerRepo.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        if (dto.name() != null) {
            player.setName(dto.name());
        }

        if (dto.age() != null) {
            player.setAge(dto.age());
        }

        if (dto.level() != null) {
            player.setLevel(dto.level());
        }

        if (dto.money() != null) {
            player.setMoney(dto.money());
        }

        if (dto.role() != null) {
            player.setRole(dto.role());
        }

        Player updatedPlayer = playerRepo.save(player);

        return playerMapper.toDto(updatedPlayer);
    }

    //nuevo metodo para añadir notas a cada jugador
    public PlayerMeDto updateMyNotes(Authentication authentication, String notes) {
        String username = authentication.getName();
        Player player = loadPlayerByName(username);
        player.setNotes(notes);
        playerRepo.save(player);
        return playerMapper.toMeDto(player);
    }

}


