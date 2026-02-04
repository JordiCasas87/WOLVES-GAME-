package com.jordi.wolves.wolves_api.question.service;
import com.jordi.wolves.wolves_api.question.exception.QuestionNotFoundException;


import com.jordi.wolves.wolves_api.question.dto.QuestionDtoResponse;
import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import com.jordi.wolves.wolves_api.question.mapper.QuestionMapper;
import com.jordi.wolves.wolves_api.question.model.Question;
import com.jordi.wolves.wolves_api.question.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class QuestionService {

    private QuestionRepository questionRepository;
    private QuestionMapper questionMapper;
    private static final int QUESTIONS_PER_GAME = 10;

    public QuestionService(QuestionRepository questionRepository, QuestionMapper questionMapper) {
        this.questionRepository = questionRepository;
        this.questionMapper = questionMapper;
    }

    public List<QuestionDtoResponse> getRandomQuestions() {

        List<Question> questions = questionRepository.findAll();

        Collections.shuffle(questions);

        return questions.stream()
                .limit(QUESTIONS_PER_GAME)
                .map(questionMapper::toDto)
                .toList();
    }


    public List<Question> getRandomQuestionEntitiesByDifficulty(Difficulty difficulty) {
        List<Question> questions = questionRepository.findByDifficulty(difficulty);
        Collections.shuffle(questions);
        return questions.stream()
                .limit(QUESTIONS_PER_GAME)
                .toList();
    }


    public List<Question> getRandomQuestionsByIds(List<String> ids, int amount) {

        List<Question> questions = questionRepository.findByIdIn(ids);
        Collections.shuffle(questions);

        return questions.stream()
                .limit(amount)
                .toList();
    }

    public void deleteQuestionById(String id) {
        if (!questionRepository.existsById(id)) {
            throw new QuestionNotFoundException("Question not found with id: " + id);
        }
        questionRepository.deleteById(id);
    }

}
