package com.jordi.wolves.wolves_api.commun;

import com.jordi.wolves.wolves_api.player.exception.PlayerNotFoundException;
import com.jordi.wolves.wolves_api.game.exception.GameNotFoundException;
import com.jordi.wolves.wolves_api.game.exception.GameAlreadyFinishedException;
import com.jordi.wolves.wolves_api.game.exception.GameLastQuestionException;
import com.jordi.wolves.wolves_api.game.exception.GameNoQuestionAsked;
import com.jordi.wolves.wolves_api.question.exception.QuestionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PlayerNotFoundException.class)
    public ResponseEntity<String> handlePlayerNotFound(PlayerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<String> handleGameNotFound(GameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(GameAlreadyFinishedException.class)
    public ResponseEntity<String> handleGameAlreadyFinished(GameAlreadyFinishedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(GameLastQuestionException.class)
    public ResponseEntity<String> handleGameLastQuestion(GameLastQuestionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(GameNoQuestionAsked.class)
    public ResponseEntity<String> handleGameNoQuestionAsked(GameNoQuestionAsked ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<String> handleQuestionNotFound(QuestionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    //añadido para validaciones

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }
}
