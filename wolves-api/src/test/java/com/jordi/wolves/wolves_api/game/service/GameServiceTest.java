package com.jordi.wolves.wolves_api.game.service;

import com.jordi.wolves.wolves_api.game.dto.GameDtoResponse;
import com.jordi.wolves.wolves_api.game.enums.GameStatus;
import com.jordi.wolves.wolves_api.game.exception.GameLastQuestionException;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
}
