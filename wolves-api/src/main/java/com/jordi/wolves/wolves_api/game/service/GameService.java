package com.jordi.wolves.wolves_api.game.service;

import com.jordi.wolves.wolves_api.game.dto.AnswerRequestDto;
import com.jordi.wolves.wolves_api.game.dto.AnswerResponseDto;
import com.jordi.wolves.wolves_api.game.dto.GameDtoResponse;
import com.jordi.wolves.wolves_api.game.dto.GameResultDto;
import com.jordi.wolves.wolves_api.game.enums.GameStatus;
import com.jordi.wolves.wolves_api.game.exception.GameAlreadyFinishedException;
import com.jordi.wolves.wolves_api.game.exception.GameLastQuestionException;
import com.jordi.wolves.wolves_api.game.exception.GameNoQuestionAsked;
import com.jordi.wolves.wolves_api.game.exception.GameNotFoundException;
import com.jordi.wolves.wolves_api.game.mapper.GameMapper;
import com.jordi.wolves.wolves_api.game.model.Game;
import com.jordi.wolves.wolves_api.game.repository.GameRepository;
import com.jordi.wolves.wolves_api.game.wolfText.WolfMessages;
import com.jordi.wolves.wolves_api.player.model.Player;
import com.jordi.wolves.wolves_api.player.service.PlayerService;
import com.jordi.wolves.wolves_api.question.dto.QuestionDtoNextResponse;
import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import com.jordi.wolves.wolves_api.question.mapper.QuestionMapper;
import com.jordi.wolves.wolves_api.question.model.Question;

import com.jordi.wolves.wolves_api.question.service.QuestionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class GameService {

    private GameRepository gameRepo;
    private QuestionService questionService;
    private QuestionMapper questionMapper;
    private GameMapper gameMapper;
    private PlayerService playerService;

    private static final List<Integer> REWARDS = List.of(1500, 2500, 5000); //lista de premios para crear partida

    public GameService(GameRepository gameRepo,
                       QuestionService questionService,
                       QuestionMapper questionMapper,
                       GameMapper gameMapper,
                       PlayerService playerService) {

        this.gameRepo = gameRepo;
        this.questionService = questionService;
        this.questionMapper = questionMapper;
        this.gameMapper = gameMapper;
        this.playerService = playerService;
    }

    public GameDtoResponse createGame(Authentication authentication, Difficulty difficulty) {

        String username = authentication.getName();

        Player player = playerService.loadPlayerByName(username);
        String playerId = player.getId();

        Optional<Game> existingGame =
                gameRepo.findFirstByPlayerIdAndStatusIn(
                        playerId,
                        List.of(GameStatus.CREATED, GameStatus.IN_PROGRESS)
                );

        if (existingGame.isPresent()) {
            return resumeExistingGame(existingGame.get());
        }

        int reward = REWARDS.get(new Random().nextInt(REWARDS.size()));

        List<Question> questions =
                questionService.getRandomQuestionEntitiesByDifficulty(difficulty);

        Game newGame = new Game(playerId, difficulty, questions, reward);

        Game savedGame = gameRepo.save(newGame);

        return gameMapper.toDto(savedGame);
    }

    public QuestionDtoNextResponse nextQuestion(String gameId) {
        Game gameFinded = gameRepo.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game Not Found!"));

        if (gameFinded.getStatus() == GameStatus.CREATED) {
            gameFinded.setStatus(GameStatus.IN_PROGRESS);
        }

        if (gameFinded.getStatus() == GameStatus.FINISHED) {
            throw new GameLastQuestionException("The game is already finished");
        }

        List<Question> gameQuestions = gameFinded.getQuestions();
        int currentIndex = gameFinded.getCurrentQuestionIndex(); //0

        if (gameFinded.isAwaitingAnswer()) {
            Question pendingQuestion = gameQuestions.get(currentIndex - 1);
            return new QuestionDtoNextResponse(
                    currentIndex,
                    pendingQuestion.getIntro(),
                    pendingQuestion.getText(),
                    pendingQuestion.getAnswers()
            );
        }

        if (currentIndex >= gameQuestions.size()) {
            throw new GameLastQuestionException("No more questions in this game");
        }

        Question nextQuestion = gameQuestions.get(currentIndex);
        gameFinded.setCurrentQuestionIndex(currentIndex + 1); //1

        gameFinded.setAwaitingAnswer(true);

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

        if (!gameFinded.isAwaitingAnswer()) {
            throw new GameNoQuestionAsked("No question to answer right now");
        }

        Question answeredQuestion = getAnsweredQuestion(gameFinded);

        boolean correct =
                answeredQuestion.getCorrectAnswerIndex() == dtoRequest.selectedAnswer();


        updateGameAfterAnswer(gameFinded, answeredQuestion, correct);
        return buildResponse(correct);

    }

    public GameResultDto getResult(String gameId) {
        String finalMessage;

        Game game = gameRepo.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found"));

        if (game.getStatus() != GameStatus.FINISHED) {
            throw new IllegalStateException("Game is not finished yet");
        }
        boolean passed = game.getScore() >= 6;

        if (passed) {
            finalMessage = WolfMessages.FINAL_PASSED;
        } else {
            finalMessage = WolfMessages.FINAL_FAILED;
        }

        return new GameResultDto(
                game.getId(),
                game.getPlayerId(),
                game.getScore(),
                game.getQuestions().size(),
                passed,
                game.getReward(),
                finalMessage
        );
    }

    public GameDtoResponse createGameWithMistakes(Authentication authentication) {

        String username = authentication.getName();
        Player player = playerService.loadPlayerByName(username);
        String playerId = player.getId();

        Optional<Game> existingGame =
                gameRepo.findFirstByPlayerIdAndStatusIn(
                        playerId,
                        List.of(GameStatus.CREATED, GameStatus.IN_PROGRESS)
                );

        if (existingGame.isPresent()) {
            return resumeExistingGame(existingGame.get());
        }
        List<String> incorrectIds = player.getIncorrectQuestionsIdList();

        if (incorrectIds.size() < 10) {
            throw new IllegalStateException(
                    "Not enough incorrect questions to start a mistakes game"
            );
        }

        List<Question> questions =
                questionService.getRandomQuestionsByIds(incorrectIds, 10);

        int reward = 0; //no premio en mistakes

        Game newGame = new Game(
                playerId,
                null, // null, no quiero estado
                questions,
                reward
        );

        Game savedGame = gameRepo.save(newGame);

        return gameMapper.toDto(savedGame);
    }

    private GameDtoResponse resumeExistingGame(Game game) {

        String message = switch (game.getStatus()) {
            case CREATED -> WolfMessages.RESUME_CREATED;
            case IN_PROGRESS -> WolfMessages.RESUME_IN_PROGRESS;
            default -> null;
        };

        return gameMapper.toDto(game, message);
    }

    private Question getAnsweredQuestion(Game game) {

        int currentIndex = game.getCurrentQuestionIndex();

        if (currentIndex == 0) {
            throw new GameNoQuestionAsked("No question has been asked yet");
        }

        return game.getQuestions().get(currentIndex - 1);
    }


    private void updateGameAfterAnswer(Game game, Question question, boolean correct) {

        if (correct) {
            game.setScore(game.getScore() + 1);

        } else {
            registerIncorrectQuestion(game, question);
        }

        if (game.getCurrentQuestionIndex() == game.getQuestions().size()) {
            game.setStatus(GameStatus.FINISHED);
            finishGame(game); //actualizamos player
        }

        game.setAwaitingAnswer(false);
        gameRepo.save(game);
    }

    private void registerIncorrectQuestion(Game game, Question question) {
        Player player = playerService.loadPlayer(game.getPlayerId());
        playerService.registerIncorrectQuestion(player, question.getId());
    }

    private AnswerResponseDto buildResponse(boolean correct) {
        if (correct) {
            return new AnswerResponseDto(
                    true,
                    WolfMessages.ANSWER_CORRECT
            );
        } else {
            return new AnswerResponseDto(
                    false,
                    WolfMessages.ANSWER_INCORRECT
            );
        }
    }

    private void finishGame(Game game) {
        // llamada a dominio player
        Player player = playerService.loadPlayer(game.getPlayerId());
        boolean passed = game.getScore() >= 6;
        playerService.applyGameResult(player, game.getReward(), passed);

    }
}


