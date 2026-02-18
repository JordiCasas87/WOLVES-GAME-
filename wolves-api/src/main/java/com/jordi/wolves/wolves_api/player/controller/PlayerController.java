package com.jordi.wolves.wolves_api.player.controller;

import com.jordi.wolves.wolves_api.player.dto.*;
import com.jordi.wolves.wolves_api.player.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class PlayerController {

    private static final Logger log = LoggerFactory.getLogger(PlayerController.class);

    private PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/players")
    public ResponseEntity<PlayerDtoResponse> createPlayer (@Valid @RequestBody PlayerDtoRequest playerDtoRequest) {
        log.info("Solicitud para crear un nuevo jugador");
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
        log.info("Solicitud para obtener la lista de jugadores");
        List<PlayerDtoResponse> allPlayerList = playerService.getAllPlayers();
        return new ResponseEntity<>(allPlayerList, HttpStatus.OK);
    }

    @DeleteMapping ("/players/{id}")
    public ResponseEntity <Void> deletePlayerById ( @PathVariable String id ){
        log.warn("Solicitud para eliminar jugador con id {}", id);
        playerService.deletePlayerById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/players/ranking")
    public List<PlayerRankingDto> getRanking() {
        return playerService.getRanking();
    }

    @GetMapping("/me")
    public PlayerMeDto getMe(Authentication authentication) {
        log.debug("Solicitud del perfil del usuario autenticado");
        return playerService.getMe(authentication);
    }

    @PutMapping("/players/{id}")
    public ResponseEntity<PlayerDtoResponse> updatePlayerByAdmin(
            @PathVariable String id,
            @RequestBody PlayerAdminUpdateDto dto
    ) {
        log.warn("Admin actualizando jugador con id {}", id);
        return ResponseEntity.ok(playerService.updateByAdmin(id, dto));
    }

   /* @PatchMapping("/me/notes")
    public ResponseEntity<PlayerMeDto> updateMyNotes(
            Authentication authentication,
            @RequestBody String notes
    ) {
        return ResponseEntity.ok(
                playerService.updateMyNotes(authentication, notes)
        );
    }*/

    @PatchMapping("/me/notes")
    public ResponseEntity<PlayerMeDto> updateMyNotes(
            Authentication authentication,
            @RequestBody PlayerNotesDto dto
    ) {
        log.info("Usuario actualizando sus notas");
        return ResponseEntity.ok(
                playerService.updateMyNotes(authentication, dto.notes())
        );
    }
}
