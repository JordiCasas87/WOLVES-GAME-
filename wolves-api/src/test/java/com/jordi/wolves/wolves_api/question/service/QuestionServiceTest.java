package com.jordi.wolves.wolves_api.question.service;

import com.jordi.wolves.wolves_api.question.dto.QuestionAdminListDto;
import com.jordi.wolves.wolves_api.question.dto.QuestionDtoResponse;
import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import com.jordi.wolves.wolves_api.question.exception.QuestionNotFoundException;
import com.jordi.wolves.wolves_api.question.mapper.QuestionMapper;
import com.jordi.wolves.wolves_api.question.model.Question;
import com.jordi.wolves.wolves_api.question.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionMapper questionMapper;

    private QuestionService questionService;

    @BeforeEach
    void setUp() {
        questionService = new QuestionService(questionRepository, questionMapper);
    }

    @Test
    void getRandomQuestionsReturnsAtMostTenMappedQuestions() {
        List<Question> questions = questions(12, Difficulty.EASY);
        List<QuestionDtoResponse> expectedDtos = questions.stream()
                .map(this::responseFor)
                .toList();
        when(questionRepository.findAll()).thenReturn(new ArrayList<>(questions));
        when(questionMapper.toDto(any(Question.class)))
                .thenAnswer(invocation -> responseFor(invocation.getArgument(0)));

        List<QuestionDtoResponse> result = questionService.getRandomQuestions();

        assertEquals(10, result.size());
        assertTrue(expectedDtos.containsAll(result));
    }

    @Test
    void getRandomQuestionsReturnsEmptyListWhenRepositoryIsEmpty() {
        when(questionRepository.findAll()).thenReturn(new ArrayList<>());

        List<QuestionDtoResponse> result = questionService.getRandomQuestions();

        assertTrue(result.isEmpty());
    }

    @Test
    void getRandomQuestionEntitiesByDifficultyUsesFilterAndLimitsResult() {
        List<Question> questions = questions(12, Difficulty.HARD);
        when(questionRepository.findByDifficulty(Difficulty.HARD))
                .thenReturn(new ArrayList<>(questions));

        List<Question> result = questionService
                .getRandomQuestionEntitiesByDifficulty(Difficulty.HARD);

        assertEquals(10, result.size());
        assertTrue(questions.containsAll(result));
        verify(questionRepository).findByDifficulty(Difficulty.HARD);
    }

    @Test
    void getRandomQuestionEntitiesByDifficultyReturnsAllWhenFewerThanTenExist() {
        List<Question> questions = questions(3, Difficulty.MEDIUM);
        when(questionRepository.findByDifficulty(Difficulty.MEDIUM))
                .thenReturn(new ArrayList<>(questions));

        List<Question> result = questionService
                .getRandomQuestionEntitiesByDifficulty(Difficulty.MEDIUM);

        assertEquals(3, result.size());
        assertTrue(result.containsAll(questions));
    }

    @Test
    void getRandomQuestionsByIdsReturnsRequestedAmountAtMost() {
        List<String> ids = List.of("q-1", "q-2", "q-3", "q-4", "q-5");
        List<Question> questions = questions(5, Difficulty.EASY);
        when(questionRepository.findByIdIn(ids)).thenReturn(new ArrayList<>(questions));

        List<Question> result = questionService.getRandomQuestionsByIds(ids, 3);

        assertEquals(3, result.size());
        assertTrue(questions.containsAll(result));
        verify(questionRepository).findByIdIn(ids);
    }

    @Test
    void deleteQuestionByIdDeletesQuestionWhenItExists() {
        when(questionRepository.existsById("q-1")).thenReturn(true);

        questionService.deleteQuestionById("q-1");

        verify(questionRepository).deleteById("q-1");
    }

    @Test
    void deleteQuestionByIdThrowsWhenQuestionDoesNotExist() {
        when(questionRepository.existsById("missing-question")).thenReturn(false);

        QuestionNotFoundException exception = assertThrows(
                QuestionNotFoundException.class,
                () -> questionService.deleteQuestionById("missing-question")
        );

        assertEquals("Question not found with id: missing-question", exception.getMessage());
        verify(questionRepository, never()).deleteById("missing-question");
    }

    @Test
    void getAllQuestionsForAdminMapsIdAndText() {
        Question first = question("q-1", Difficulty.EASY);
        Question second = question("q-2", Difficulty.HARD);
        when(questionRepository.findAll()).thenReturn(List.of(first, second));

        List<QuestionAdminListDto> result = questionService.getAllQuestionsForAdmin();

        assertEquals(
                List.of(
                        new QuestionAdminListDto("q-1", "Pregunta q-1"),
                        new QuestionAdminListDto("q-2", "Pregunta q-2")
                ),
                result
        );
    }

    private List<Question> questions(int amount, Difficulty difficulty) {
        List<Question> questions = new ArrayList<>();
        for (int index = 1; index <= amount; index++) {
            questions.add(question("q-" + index, difficulty));
        }
        return questions;
    }

    private Question question(String id, Difficulty difficulty) {
        return new Question(
                id,
                "Introducción " + id,
                "Pregunta " + id,
                List.of("A", "B", "C", "D"),
                0,
                difficulty
        );
    }

    private QuestionDtoResponse responseFor(Question question) {
        return new QuestionDtoResponse(
                question.getIntro(),
                question.getText(),
                question.getAnswers()
        );
    }
}
