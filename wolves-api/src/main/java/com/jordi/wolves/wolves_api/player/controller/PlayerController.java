package com.jordi.wolves.wolves_api.player.controller;

import com.jordi.wolves.wolves_api.player.dto.PlayerDtoRequest;
import com.jordi.wolves.wolves_api.player.dto.PlayerDtoResponse;
import com.jordi.wolves.wolves_api.player.dto.PlayerMeDto;
import com.jordi.wolves.wolves_api.player.dto.PlayerRankingDto;
import com.jordi.wolves.wolves_api.player.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
public class PlayerController {

    private PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/players")
    public ResponseEntity<PlayerDtoResponse> createPlayer (@Valid @RequestBody PlayerDtoRequest playerDtoRequest) {
        PlayerDtoResponse newPlayerDto = playerService.createPlayer(playerDtoRequest);

        return new ResponseEntity<>(newPlayerDto, HttpStatus.CREATED);
    }


    @GetMapping("/players/{id}")
    public ResponseEntity<PlayerDtoResponse> getPlayerById ( @PathVariable String id ){
        PlayerDtoResponse dtoPlayer =playerService.getPlayerById(id);
        return new ResponseEntity<>(dtoPlayer, HttpStatus.OK);
    }


   @GetMapping("/players")
    public ResponseEntity <List<PlayerDtoResponse>> getAllPlayers () {

        List<PlayerDtoResponse> allPlayerList = playerService.getAllPlayers();

        return new ResponseEntity<>(allPlayerList, HttpStatus.OK);
    }



    @DeleteMapping ("/players/{id}")
    public ResponseEntity <Void> deletePlayerById ( @PathVariable String id ){
        playerService.deletePlayerById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @GetMapping("/players/ranking")
    public List<PlayerRankingDto> getRanking() {
        return playerService.getRanking();
    }


    @GetMapping("/me")
    public PlayerMeDto getMe(Authentication authentication) {
        return playerService.getMe(authentication);
    }

    // TODO: Endpoint disponible cuando se implemente JWT
// @GetMapping("/me")
// public PlayerDtoResponse getMyPlayer(Authentication authentication) {
//     // El player se obtendrá a partir del usuario autenticado (JWT)
// }

}
