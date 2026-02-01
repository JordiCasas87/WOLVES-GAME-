package com.jordi.wolves.wolves_api.question.controller;



import com.jordi.wolves.wolves_api.question.dto.QuestionDtoResponse;
import com.jordi.wolves.wolves_api.question.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class QuestionController {

private QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/questions/random")
    public ResponseEntity <List<QuestionDtoResponse>> getRandomQuestions (){
        List<QuestionDtoResponse>  dtoListQuestion = questionService.getRandomQuestions();
        return new ResponseEntity<>(dtoListQuestion, HttpStatus.OK);
    }

    /*
    @GetMapping("/questions/easy")
public ResponseEntity<List<QuestionDtoResponse>> getEasyQuestions() {
    return ResponseEntity.ok(questionService.getQuestionsByDifficulty(EASY));
}

@GetMapping("/questions/medium")
public ResponseEntity<List<QuestionDtoResponse>> getMediumQuestions() {
    return ResponseEntity.ok(questionService.getQuestionsByDifficulty(MEDIUM));
}

@GetMapping("/questions/hard")
public ResponseEntity<List<QuestionDtoResponse>> getHardQuestions() {
    return ResponseEntity.ok(questionService.getQuestionsByDifficulty(HARD));
}
     */



}
