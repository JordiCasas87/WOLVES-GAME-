package com.jordi.wolves.wolves_api.question.controller;

import com.jordi.wolves.wolves_api.question.dto.QuestionAdminListDto;
import com.jordi.wolves.wolves_api.question.dto.QuestionDtoResponse;
import com.jordi.wolves.wolves_api.question.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String id) {
        questionService.deleteQuestionById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/questions")
    public ResponseEntity<List<QuestionAdminListDto>> getAllQuestions() {
        return ResponseEntity.ok(
                questionService.getAllQuestionsForAdmin()
        );
    }

}
