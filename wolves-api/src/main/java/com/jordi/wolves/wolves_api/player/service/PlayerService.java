package com.jordi.wolves.wolves_api.player.service;

import com.jordi.wolves.wolves_api.player.dto.*;
import com.jordi.wolves.wolves_api.player.exception.PlayerNotFoundException;
import com.jordi.wolves.wolves_api.player.mapper.PlayerMapper;
import com.jordi.wolves.wolves_api.player.model.Player;
import com.jordi.wolves.wolves_api.player.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

//añado imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

// imports necesarios para Cache!
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;


@Service
public class PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);

    private final PlayerRepository playerRepo;
    private final PlayerMapper playerMapper;

    public PlayerService(PlayerRepository playerRepo, PlayerMapper playerMapper) {
        this.playerRepo = playerRepo;
        this.playerMapper = playerMapper;
    }

    // cuidado si algo cambia, evitar que la aplicación devuelva datos antiguos o desactualizados cuando algo cambia.
    @CacheEvict(value = {"players", "ranking"}, allEntries = true)
    public PlayerDtoResponse createPlayer(PlayerDtoRequest playerDtoRequest) {

        log.info("Creando nuevo jugador con nombre: {}", playerDtoRequest.name());
        Player newPlayer = playerMapper.toEntity(playerDtoRequest);
        Player saved = playerRepo.save(newPlayer);
        log.info("Jugador creado correctamente con id: {}", saved.getId());
        return playerMapper.toDto(saved);


    }

    public PlayerDtoResponse getPlayerById ( String id){

        Optional <Player> findedPlayer = playerRepo.findById(id);
        if (findedPlayer.isEmpty()){
            log.warn("Jugador no encontrado con id: {}", id);
            throw new PlayerNotFoundException("Player not found");
        }
        Player findedPlayerObject = findedPlayer.get();

       return playerMapper.toDto(findedPlayerObject);

    }

    // cache aqui.
    @Cacheable("players")
    public List<PlayerDtoResponse> getAllPlayers (){
        log.debug("Solicitando listado completo de jugadores");
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

    @Cacheable("ranking")
    public List<PlayerRankingDto> getRanking() {
        log.debug("Calculando ranking de jugadores");
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

    @CacheEvict(value = {"players", "ranking"}, allEntries = true)
    public void applyGameResult(Player player, int reward, boolean passed) {
        player.incrementGamesPlayed();

        if (passed) {
            player.incrementLevel();
            player.incrementMoney(reward);
        }

        playerRepo.save(player);
    }
    public PlayerMeDto getMe(Authentication authentication) {
        log.debug("Obteniendo perfil del jugador autenticado: {}", authentication.getName());
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

    public PlayerMeDto updateMyNotes(Authentication authentication, String notes) {
        String username = authentication.getName();
        Player player = loadPlayerByName(username);
        player.setNotes(notes);
        playerRepo.save(player);
        return playerMapper.toMeDto(player);
    }

}


