package com.jordi.wolves.wolves_api.commun;

import com.jordi.wolves.wolves_api.game.exception.GameAlreadyFinishedException;
import com.jordi.wolves.wolves_api.game.exception.GameLastQuestionException;
import com.jordi.wolves.wolves_api.game.exception.GameNoQuestionAsked;
import com.jordi.wolves.wolves_api.game.exception.GameNotFoundException;
import com.jordi.wolves.wolves_api.player.exception.PlayerNotFoundException;
import com.jordi.wolves.wolves_api.question.exception.QuestionNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesPlayerNotFound() {
        assertResponse(handler.handlePlayerNotFound(
                new PlayerNotFoundException("Player not found")), HttpStatus.NOT_FOUND, "Player not found");
    }

    @Test
    void handlesGameNotFound() {
        assertResponse(handler.handleGameNotFound(
                new GameNotFoundException("Game not found")), HttpStatus.NOT_FOUND, "Game not found");
    }

    @Test
    void handlesAlreadyFinishedGame() {
        assertResponse(handler.handleGameAlreadyFinished(
                new GameAlreadyFinishedException("Game finished")), HttpStatus.CONFLICT, "Game finished");
    }

    @Test
    void handlesLastQuestion() {
        assertResponse(handler.handleGameLastQuestion(
                new GameLastQuestionException("No more questions")), HttpStatus.CONFLICT, "No more questions");
    }

    @Test
    void handlesMissingQuestionToAnswer() {
        assertResponse(handler.handleGameNoQuestionAsked(
                new GameNoQuestionAsked("No question asked")), HttpStatus.BAD_REQUEST, "No question asked");
    }

    @Test
    void handlesIllegalState() {
        assertResponse(handler.handleIllegalState(
                new IllegalStateException("Invalid state")), HttpStatus.BAD_REQUEST, "Invalid state");
    }

    @Test
    void handlesQuestionNotFound() {
        assertResponse(handler.handleQuestionNotFound(
                new QuestionNotFoundException("Question not found")), HttpStatus.NOT_FOUND, "Question not found");
    }

    @Test
    void handlesValidationErrorsByField() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("player", "name", "Name is required"),
                new FieldError("player", "password", "Password is too short")
        ));

        ResponseEntity<Map<String, String>> response = handler.handleValidationErrors(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of(
                "name", "Name is required",
                "password", "Password is too short"
        ), response.getBody());
    }

    private void assertResponse(ResponseEntity<String> response, HttpStatus status, String body) {
        assertEquals(status, response.getStatusCode());
        assertEquals(body, response.getBody());
    }
}
