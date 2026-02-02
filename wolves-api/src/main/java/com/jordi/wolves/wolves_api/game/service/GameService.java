package com.jordi.wolves.wolves_api.game.service;

import com.jordi.wolves.wolves_api.game.dto.AnswerRequestDto;
import com.jordi.wolves.wolves_api.game.dto.AnswerResponseDto;
import com.jordi.wolves.wolves_api.game.dto.GameDtoResponse;
import com.jordi.wolves.wolves_api.game.enums.GameStatus;
import com.jordi.wolves.wolves_api.game.exception.GameAlreadyFinishedException;
import com.jordi.wolves.wolves_api.game.exception.GameLastQuestionException;
import com.jordi.wolves.wolves_api.game.exception.GameNoQuestionAsked;
import com.jordi.wolves.wolves_api.game.exception.GameNotFoundException;
import com.jordi.wolves.wolves_api.game.mapper.GameMapper;
import com.jordi.wolves.wolves_api.game.model.Game;
import com.jordi.wolves.wolves_api.game.repository.GameRepository;
import com.jordi.wolves.wolves_api.question.dto.QuestionDtoNextResponse;
import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import com.jordi.wolves.wolves_api.question.mapper.QuestionMapper;
import com.jordi.wolves.wolves_api.question.model.Question;

import com.jordi.wolves.wolves_api.question.service.QuestionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    GameRepository gameRepo;
    QuestionService questionService;
    QuestionMapper questionMapper;
    GameMapper gameMapper;

    public GameService(GameRepository gameRepo,
                       QuestionService questionService,
                       QuestionMapper questionMapper,
                       GameMapper gameMapper) {

        this.gameRepo = gameRepo;
        this.questionService = questionService;
        this.questionMapper = questionMapper;
        this.gameMapper = gameMapper;
    }

    public GameDtoResponse createGame(String playerId, Difficulty difficulty) {

        List<Question> questions =
                questionService.getRandomQuestionEntitiesByDifficulty(difficulty);

        Game newGame = new Game(playerId, difficulty, questions);

        Game savedGame = gameRepo.save(newGame);

        return gameMapper.toDto(savedGame);
    }

    public QuestionDtoNextResponse nextQuestion(String gameId) {
        Game gameFinded = gameRepo.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game Not Found!"));

        //cambiar estado aqui?
        if (gameFinded.getStatus() == GameStatus.CREATED) {
            gameFinded.setStatus(GameStatus.IN_PROGRESS);
        }
        //si esta acabado que no se vuelva a pedir un pregunta mas o dará error
        if (gameFinded.getStatus() == GameStatus.FINISHED) {
            throw new GameLastQuestionException("The game is already finished");
        }

        List<Question> gameQuestions = gameFinded.getQuestions();
        int currentIndex = gameFinded.getCurrentQuestionIndex(); //0

        //para detener la partida llegado a 10.
        if (currentIndex >= gameQuestions.size()) {
            throw new GameLastQuestionException("No more questions in this game");
        }

        Question nextQuestion = gameQuestions.get(currentIndex);
        gameFinded.setCurrentQuestionIndex(currentIndex + 1); //1
        gameRepo.save(gameFinded);

        return new QuestionDtoNextResponse(
                currentIndex + 1,
                nextQuestion.getIntro(),
                nextQuestion.getText(),
                nextQuestion.getAnswers());

    }


    public AnswerResponseDto answerQuestion(String gameId, AnswerRequestDto dtoRequest) {

        Game gameFinded = gameRepo.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game Not Found!"));

        if (gameFinded.getStatus() == GameStatus.FINISHED) {
            throw new GameAlreadyFinishedException("The game is already finished");
        }

        Question answeredQuestion = getAnsweredQuestion(gameFinded);

        boolean correct =
                answeredQuestion.getCorrectAnswerIndex() == dtoRequest.selectedAnswer();


        updateGameAfterAnswer(gameFinded, correct);

        return buildResponse(correct);

    }


    //métodos para esta clase.

    private Question getAnsweredQuestion(Game game) {

        int currentIndex = game.getCurrentQuestionIndex();

        if (currentIndex == 0) {
            throw new GameNoQuestionAsked("No question has been asked yet");
        }

        return game.getQuestions().get(currentIndex - 1);
    }


    private void updateGameAfterAnswer(Game game, boolean correct) {

        if (correct) {
            game.setScore(game.getScore() + 1);
        }
        // para terminar partida cuando llega a 10
        if (game.getCurrentQuestionIndex() == game.getQuestions().size()) {
            game.setStatus(GameStatus.FINISHED);
        }

        gameRepo.save(game);
    }

    private AnswerResponseDto buildResponse(boolean correct) {
        if (correct) {
            return new AnswerResponseDto(
                    true,
                    "mmm... Correcto, sigamos"
            );
        } else {
            return new AnswerResponseDto(
                    false,
                    "Eso no es correcto, estás a un paso de mi sartén"
            );

        }
    }

}



/*

GameResultDto getResult(String gameId);
 */




