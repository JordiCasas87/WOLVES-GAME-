package com.jordi.wolves.wolves_api.game.controller;


import com.jordi.wolves.wolves_api.game.dto.AnswerRequestDto;
import com.jordi.wolves.wolves_api.game.dto.AnswerResponseDto;
import com.jordi.wolves.wolves_api.game.dto.GameDtoResponse;
import com.jordi.wolves.wolves_api.game.dto.GameResultDto;
import com.jordi.wolves.wolves_api.game.service.GameService;
import com.jordi.wolves.wolves_api.question.dto.QuestionDtoNextResponse;
import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class GameController {

    private GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }


    @PostMapping("/game/new")
    public ResponseEntity<GameDtoResponse> createGame(@RequestParam String playerId,@RequestParam Difficulty difficulty) {
        GameDtoResponse dtoGameOut = gameService.createGame(playerId, difficulty);

        return new ResponseEntity<GameDtoResponse>(dtoGameOut, HttpStatus.CREATED);
    }

    @GetMapping("/game/{id}/nextQuestion")
    public ResponseEntity<QuestionDtoNextResponse> nextQuestion (@PathVariable String id){
        QuestionDtoNextResponse questionDtoNextResponse = gameService.nextQuestion(id);
        return new ResponseEntity<QuestionDtoNextResponse>(questionDtoNextResponse, HttpStatus.OK);
    }

    @PostMapping("/game/{id}/answer")
    public ResponseEntity <AnswerResponseDto> answerQuestion (@PathVariable String id, @RequestBody AnswerRequestDto dtoRequest){
        AnswerResponseDto dtoResponse = gameService.answerQuestion (id, dtoRequest);

        return new ResponseEntity<AnswerResponseDto> ( dtoResponse, HttpStatus.OK);
    }

    @GetMapping("/game/{id}/result")
    public ResponseEntity<GameResultDto> getResult(@PathVariable String id) {
        return ResponseEntity.ok(gameService.getResult(id));
    }


    //endpoints

    /*

	3.	POST /games/{id}/answer
	4.	GET /games/{id}/result
	5.	(opcional) POST /games/{id}/abandon

     */





}
