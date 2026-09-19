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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    private static final String USERNAME = "jordi";
    private static final String PLAYER_ID = "player-1";
    private static final List<Integer> VALID_REWARDS = List.of(1500, 2500, 5000);

    @Mock
    private GameRepository gameRepository;

    @Mock
    private QuestionService questionService;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private GameMapper gameMapper;

    @Mock
    private PlayerService playerService;

    @Mock
    private Authentication authentication;

    private GameService gameService;
    private Player player;

    @BeforeEach
    void setUp() {
        gameService = new GameService(
                gameRepository,
                questionService,
                questionMapper,
                gameMapper,
                playerService
        );

        player = new Player();
        player.setId(PLAYER_ID);
    }

    @Test
    void createGameCreatesAndReturnsANewGameWhenPlayerHasNoActiveGame() {
        Difficulty difficulty = Difficulty.EASY;
        List<Question> questions = List.of(new Question(), new Question());
        GameDtoResponse expectedResponse = new GameDtoResponse(
                "game-1",
                PLAYER_ID,
                difficulty,
                GameStatus.CREATED,
                null
        );

        when(authentication.getName()).thenReturn(USERNAME);
        when(playerService.loadPlayerByName(USERNAME)).thenReturn(player);
        when(gameRepository.findFirstByPlayerIdAndStatusIn(
                PLAYER_ID,
                List.of(GameStatus.CREATED, GameStatus.IN_PROGRESS)
        )).thenReturn(Optional.empty());
        when(questionService.getRandomQuestionEntitiesByDifficulty(difficulty)).thenReturn(questions);
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameMapper.toDto(any(Game.class))).thenReturn(expectedResponse);

        GameDtoResponse result = gameService.createGame(authentication, difficulty);

        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(gameCaptor.capture());
        Game savedGame = gameCaptor.getValue();

        assertSame(expectedResponse, result);
        assertEquals(PLAYER_ID, savedGame.getPlayerId());
        assertEquals(difficulty, savedGame.getDifficulty());
        assertSame(questions, savedGame.getQuestions());
        assertEquals(GameStatus.CREATED, savedGame.getStatus());
        assertEquals(0, savedGame.getCurrentQuestionIndex());
        assertEquals(0, savedGame.getScore());
        assertFalse(savedGame.isAwaitingAnswer());
        assertTrue(VALID_REWARDS.contains(savedGame.getReward()));
        verify(gameMapper).toDto(savedGame);
    }

    @Test
    void createGameResumesExistingCreatedGame() {
        Game existingGame = existingGameWithStatus(GameStatus.CREATED);
        GameDtoResponse expectedResponse = new GameDtoResponse(
                "game-1",
                PLAYER_ID,
                Difficulty.EASY,
                GameStatus.CREATED,
                WolfMessages.RESUME_CREATED
        );

        prepareExistingGame(existingGame);
        when(gameMapper.toDto(existingGame, WolfMessages.RESUME_CREATED)).thenReturn(expectedResponse);

        GameDtoResponse result = gameService.createGame(authentication, Difficulty.EASY);

        assertSame(expectedResponse, result);
        verify(gameMapper).toDto(existingGame, WolfMessages.RESUME_CREATED);
        verify(questionService, never()).getRandomQuestionEntitiesByDifficulty(any());
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void createGameResumesExistingGameInProgress() {
        Game existingGame = existingGameWithStatus(GameStatus.IN_PROGRESS);
        GameDtoResponse expectedResponse = new GameDtoResponse(
                "game-1",
                PLAYER_ID,
                Difficulty.EASY,
                GameStatus.IN_PROGRESS,
                WolfMessages.RESUME_IN_PROGRESS
        );

        prepareExistingGame(existingGame);
        when(gameMapper.toDto(existingGame, WolfMessages.RESUME_IN_PROGRESS)).thenReturn(expectedResponse);

        GameDtoResponse result = gameService.createGame(authentication, Difficulty.EASY);

        assertSame(expectedResponse, result);
        verify(gameMapper).toDto(existingGame, WolfMessages.RESUME_IN_PROGRESS);
        verify(questionService, never()).getRandomQuestionEntitiesByDifficulty(any());
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void nextQuestionReturnsFirstQuestionAndStartsGame() {
        Question firstQuestion = question(
                "question-1",
                "Primera introducción",
                "Primera pregunta",
                List.of("A", "B", "C", "D")
        );
        Game game = new Game(PLAYER_ID, Difficulty.EASY, List.of(firstQuestion), 1500);
        game.setId("game-1");
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

        QuestionDtoNextResponse result = gameService.nextQuestion("game-1");

        assertEquals(1, result.numberQuestion());
        assertEquals(firstQuestion.getIntro(), result.intro());
        assertEquals(firstQuestion.getText(), result.text());
        assertSame(firstQuestion.getAnswers(), result.answers());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals(1, game.getCurrentQuestionIndex());
        assertTrue(game.isAwaitingAnswer());
        verify(gameRepository).save(game);
    }

    @Test
    void nextQuestionReturnsPendingQuestionWithoutAdvancingGame() {
        Question firstQuestion = question(
                "question-1",
                "Primera introducción",
                "Primera pregunta",
                List.of("A", "B", "C", "D")
        );
        Question secondQuestion = question(
                "question-2",
                "Segunda introducción",
                "Segunda pregunta",
                List.of("E", "F", "G", "H")
        );
        Game game = new Game(
                PLAYER_ID,
                Difficulty.EASY,
                List.of(firstQuestion, secondQuestion),
                1500
        );
        game.setId("game-1");
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setCurrentQuestionIndex(1);
        game.setAwaitingAnswer(true);
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

        QuestionDtoNextResponse result = gameService.nextQuestion("game-1");

        assertEquals(1, result.numberQuestion());
        assertEquals(firstQuestion.getIntro(), result.intro());
        assertEquals(firstQuestion.getText(), result.text());
        assertSame(firstQuestion.getAnswers(), result.answers());
        assertEquals(1, game.getCurrentQuestionIndex());
        assertTrue(game.isAwaitingAnswer());
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void nextQuestionThrowsWhenGameDoesNotExist() {
        when(gameRepository.findById("missing-game")).thenReturn(Optional.empty());

        GameNotFoundException exception = assertThrows(
                GameNotFoundException.class,
                () -> gameService.nextQuestion("missing-game")
        );

        assertEquals("Game Not Found!", exception.getMessage());
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void nextQuestionThrowsWhenGameIsFinished() {
        Game game = new Game(
                PLAYER_ID,
                Difficulty.EASY,
                List.of(question("question-1", "Intro", "Pregunta", List.of("A", "B"))),
                1500
        );
        game.setStatus(GameStatus.FINISHED);
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

        GameLastQuestionException exception = assertThrows(
                GameLastQuestionException.class,
                () -> gameService.nextQuestion("game-1")
        );

        assertEquals("The game is already finished", exception.getMessage());
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void nextQuestionThrowsWhenThereAreNoQuestionsLeft() {
        Question onlyQuestion = question(
                "question-1",
                "Introducción",
                "Pregunta",
                List.of("A", "B")
        );
        Game game = new Game(PLAYER_ID, Difficulty.EASY, List.of(onlyQuestion), 1500);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setCurrentQuestionIndex(1);
        game.setAwaitingAnswer(false);
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

        GameLastQuestionException exception = assertThrows(
                GameLastQuestionException.class,
                () -> gameService.nextQuestion("game-1")
        );

        assertEquals("No more questions in this game", exception.getMessage());
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void answerQuestionIncrementsScoreWhenAnswerIsCorrect() {
        Question answeredQuestion = question(
                "question-1",
                "Introducción",
                "Pregunta",
                List.of("Correcta", "Incorrecta")
        );
        Game game = gameAwaitingAnswer(
                List.of(
                        answeredQuestion,
                        question("question-2", "Introducción", "Otra pregunta", List.of("A", "B"))
                ),
                1
        );
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

        AnswerResponseDto result = gameService.answerQuestion("game-1", new AnswerRequestDto(0));

        assertTrue(result.correct());
        assertEquals(WolfMessages.ANSWER_CORRECT, result.wolfMessage());
        assertEquals(1, game.getScore());
        assertFalse(game.isAwaitingAnswer());
        verify(gameRepository).save(game);
        verify(playerService, never()).registerIncorrectQuestion(any(), any());
        verify(playerService, never()).applyGameResult(any(), anyInt(), anyBoolean());
    }

    @Test
    void answerQuestionRegistersQuestionWhenAnswerIsIncorrect() {
        Question answeredQuestion = question(
                "question-1",
                "Introducción",
                "Pregunta",
                List.of("Correcta", "Incorrecta")
        );
        Game game = gameAwaitingAnswer(
                List.of(
                        answeredQuestion,
                        question("question-2", "Introducción", "Otra pregunta", List.of("A", "B"))
                ),
                1
        );
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));
        when(playerService.loadPlayer(PLAYER_ID)).thenReturn(player);

        AnswerResponseDto result = gameService.answerQuestion("game-1", new AnswerRequestDto(1));

        assertFalse(result.correct());
        assertEquals(WolfMessages.ANSWER_INCORRECT, result.wolfMessage());
        assertEquals(0, game.getScore());
        assertFalse(game.isAwaitingAnswer());
        verify(playerService).registerIncorrectQuestion(player, "question-1");
        verify(gameRepository).save(game);
    }

    @Test
    void answerQuestionFinishesGameAndAppliesPassedResultAfterLastCorrectAnswer() {
        Question lastQuestion = question(
                "question-1",
                "Introducción",
                "Pregunta final",
                List.of("Correcta", "Incorrecta")
        );
        Game game = gameAwaitingAnswer(List.of(lastQuestion), 1);
        game.setScore(5);
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));
        when(playerService.loadPlayer(PLAYER_ID)).thenReturn(player);

        AnswerResponseDto result = gameService.answerQuestion("game-1", new AnswerRequestDto(0));

        assertTrue(result.correct());
        assertEquals(6, game.getScore());
        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertFalse(game.isAwaitingAnswer());
        verify(playerService).applyGameResult(player, 1500, true);
        verify(gameRepository).save(game);
    }

    @Test
    void answerQuestionFinishesGameAndAppliesFailedResultAfterLastIncorrectAnswer() {
        Question lastQuestion = question(
                "question-1",
                "Introducción",
                "Pregunta final",
                List.of("Correcta", "Incorrecta")
        );
        Game game = gameAwaitingAnswer(List.of(lastQuestion), 1);
        game.setScore(5);
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));
        when(playerService.loadPlayer(PLAYER_ID)).thenReturn(player);

        AnswerResponseDto result = gameService.answerQuestion("game-1", new AnswerRequestDto(1));

        assertFalse(result.correct());
        assertEquals(5, game.getScore());
        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertFalse(game.isAwaitingAnswer());
        verify(playerService).registerIncorrectQuestion(player, "question-1");
        verify(playerService).applyGameResult(player, 1500, false);
        verify(gameRepository).save(game);
    }

    @Test
    void answerQuestionThrowsWhenGameDoesNotExist() {
        when(gameRepository.findById("missing-game")).thenReturn(Optional.empty());

        GameNotFoundException exception = assertThrows(
                GameNotFoundException.class,
                () -> gameService.answerQuestion("missing-game", new AnswerRequestDto(0))
        );

        assertEquals("Game Not Found!", exception.getMessage());
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void answerQuestionThrowsWhenGameIsFinished() {
        Game game = gameAwaitingAnswer(
                List.of(question("question-1", "Intro", "Pregunta", List.of("A", "B"))),
                1
        );
        game.setStatus(GameStatus.FINISHED);
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

        GameAlreadyFinishedException exception = assertThrows(
                GameAlreadyFinishedException.class,
                () -> gameService.answerQuestion("game-1", new AnswerRequestDto(0))
        );

        assertEquals("The game is already finished", exception.getMessage());
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void answerQuestionThrowsWhenNoQuestionIsAwaitingAnswer() {
        Game game = new Game(
                PLAYER_ID,
                Difficulty.EASY,
                List.of(question("question-1", "Intro", "Pregunta", List.of("A", "B"))),
                1500
        );
        game.setStatus(GameStatus.IN_PROGRESS);
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

        GameNoQuestionAsked exception = assertThrows(
                GameNoQuestionAsked.class,
                () -> gameService.answerQuestion("game-1", new AnswerRequestDto(0))
        );

        assertEquals("No question to answer right now", exception.getMessage());
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void answerQuestionThrowsWhenQuestionIndexHasNotAdvanced() {
        Game game = gameAwaitingAnswer(
                List.of(question("question-1", "Intro", "Pregunta", List.of("A", "B"))),
                0
        );
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

        GameNoQuestionAsked exception = assertThrows(
                GameNoQuestionAsked.class,
                () -> gameService.answerQuestion("game-1", new AnswerRequestDto(0))
        );

        assertEquals("No question has been asked yet", exception.getMessage());
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void getResultReturnsPassedResultWhenScoreReachesThreshold() {
        Game game = finishedGameWithScore(6);
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

        GameResultDto result = gameService.getResult("game-1");

        assertEquals("game-1", result.gameId());
        assertEquals(PLAYER_ID, result.playerId());
        assertEquals(6, result.score());
        assertEquals(2, result.totalQuestions());
        assertTrue(result.passed());
        assertEquals(1500, result.reward());
        assertEquals(WolfMessages.FINAL_PASSED, result.finalMessage());
    }

    @Test
    void getResultReturnsFailedResultWhenScoreIsBelowThreshold() {
        Game game = finishedGameWithScore(5);
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

        GameResultDto result = gameService.getResult("game-1");

        assertEquals("game-1", result.gameId());
        assertEquals(PLAYER_ID, result.playerId());
        assertEquals(5, result.score());
        assertEquals(2, result.totalQuestions());
        assertFalse(result.passed());
        assertEquals(1500, result.reward());
        assertEquals(WolfMessages.FINAL_FAILED, result.finalMessage());
    }

    @Test
    void getResultThrowsWhenGameDoesNotExist() {
        when(gameRepository.findById("missing-game")).thenReturn(Optional.empty());

        GameNotFoundException exception = assertThrows(
                GameNotFoundException.class,
                () -> gameService.getResult("missing-game")
        );

        assertEquals("Game not found", exception.getMessage());
    }

    @Test
    void getResultThrowsWhenGameIsNotFinished() {
        Game game = existingGameWithStatus(GameStatus.IN_PROGRESS);
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gameService.getResult("game-1")
        );

        assertEquals("Game is not finished yet", exception.getMessage());
    }

    @Test
    void createGameWithMistakesCreatesGameFromIncorrectQuestions() {
        List<String> incorrectQuestionIds = List.of(
                "question-1", "question-2", "question-3", "question-4", "question-5",
                "question-6", "question-7", "question-8", "question-9", "question-10"
        );
        List<Question> questions = incorrectQuestionIds.stream()
                .map(id -> question(id, "Intro", "Pregunta", List.of("A", "B")))
                .toList();
        GameDtoResponse expectedResponse = new GameDtoResponse(
                "mistakes-game-1",
                PLAYER_ID,
                null,
                GameStatus.CREATED,
                null
        );
        player.setIncorrectQuestionsIdList(incorrectQuestionIds);
        when(authentication.getName()).thenReturn(USERNAME);
        when(playerService.loadPlayerByName(USERNAME)).thenReturn(player);
        when(gameRepository.findFirstByPlayerIdAndStatusIn(
                PLAYER_ID,
                List.of(GameStatus.CREATED, GameStatus.IN_PROGRESS)
        )).thenReturn(Optional.empty());
        when(questionService.getRandomQuestionsByIds(incorrectQuestionIds, 10)).thenReturn(questions);
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameMapper.toDto(any(Game.class))).thenReturn(expectedResponse);

        GameDtoResponse result = gameService.createGameWithMistakes(authentication);

        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(gameCaptor.capture());
        Game savedGame = gameCaptor.getValue();
        assertSame(expectedResponse, result);
        assertEquals(PLAYER_ID, savedGame.getPlayerId());
        assertNull(savedGame.getDifficulty());
        assertSame(questions, savedGame.getQuestions());
        assertEquals(0, savedGame.getReward());
        assertEquals(GameStatus.CREATED, savedGame.getStatus());
        verify(questionService).getRandomQuestionsByIds(incorrectQuestionIds, 10);
        verify(gameMapper).toDto(savedGame);
    }

    @Test
    void createGameWithMistakesResumesExistingActiveGame() {
        Game existingGame = existingGameWithStatus(GameStatus.IN_PROGRESS);
        GameDtoResponse expectedResponse = new GameDtoResponse(
                "game-1",
                PLAYER_ID,
                Difficulty.EASY,
                GameStatus.IN_PROGRESS,
                WolfMessages.RESUME_IN_PROGRESS
        );
        prepareExistingGame(existingGame);
        when(gameMapper.toDto(existingGame, WolfMessages.RESUME_IN_PROGRESS)).thenReturn(expectedResponse);

        GameDtoResponse result = gameService.createGameWithMistakes(authentication);

        assertSame(expectedResponse, result);
        verify(gameMapper).toDto(existingGame, WolfMessages.RESUME_IN_PROGRESS);
        verify(questionService, never()).getRandomQuestionsByIds(any(), anyInt());
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void createGameWithMistakesThrowsWhenPlayerHasFewerThanTenIncorrectQuestions() {
        List<String> incorrectQuestionIds = List.of(
                "question-1", "question-2", "question-3", "question-4", "question-5",
                "question-6", "question-7", "question-8", "question-9"
        );
        player.setIncorrectQuestionsIdList(incorrectQuestionIds);
        when(authentication.getName()).thenReturn(USERNAME);
        when(playerService.loadPlayerByName(USERNAME)).thenReturn(player);
        when(gameRepository.findFirstByPlayerIdAndStatusIn(
                PLAYER_ID,
                List.of(GameStatus.CREATED, GameStatus.IN_PROGRESS)
        )).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gameService.createGameWithMistakes(authentication)
        );

        assertEquals(
                "Not enough incorrect questions to start a mistakes game",
                exception.getMessage()
        );
        verify(questionService, never()).getRandomQuestionsByIds(any(), anyInt());
        verify(gameRepository, never()).save(any(Game.class));
    }

    private void prepareExistingGame(Game existingGame) {
        when(authentication.getName()).thenReturn(USERNAME);
        when(playerService.loadPlayerByName(USERNAME)).thenReturn(player);
        when(gameRepository.findFirstByPlayerIdAndStatusIn(
                eq(PLAYER_ID),
                eq(List.of(GameStatus.CREATED, GameStatus.IN_PROGRESS))
        )).thenReturn(Optional.of(existingGame));
    }

    private Game existingGameWithStatus(GameStatus status) {
        Game game = new Game(PLAYER_ID, Difficulty.EASY, List.of(new Question()), 1500);
        game.setId("game-1");
        game.setStatus(status);
        return game;
    }

    private Question question(String id, String intro, String text, List<String> answers) {
        return new Question(id, intro, text, answers, 0, Difficulty.EASY);
    }

    private Game gameAwaitingAnswer(List<Question> questions, int currentQuestionIndex) {
        Game game = new Game(PLAYER_ID, Difficulty.EASY, questions, 1500);
        game.setId("game-1");
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setCurrentQuestionIndex(currentQuestionIndex);
        game.setAwaitingAnswer(true);
        return game;
    }

    private Game finishedGameWithScore(int score) {
        Game game = new Game(
                PLAYER_ID,
                Difficulty.EASY,
                List.of(
                        question("question-1", "Intro", "Primera pregunta", List.of("A", "B")),
                        question("question-2", "Intro", "Segunda pregunta", List.of("A", "B"))
                ),
                1500
        );
        game.setId("game-1");
        game.setScore(score);
        game.setStatus(GameStatus.FINISHED);
        return game;
    }
}
