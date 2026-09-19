package com.jordi.wolves.wolves_api.question.mapper;

import com.jordi.wolves.wolves_api.question.dto.QuestionDtoResponse;
import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import com.jordi.wolves.wolves_api.question.model.Question;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class QuestionMapperTest {

    private final QuestionMapper questionMapper = new QuestionMapper();

    @Test
    void toDtoMapsQuestionWithoutExposingCorrectAnswer() {
        List<String> answers = List.of("A", "B", "C", "D");
        Question question = new Question(
                "question-1",
                "Introducción",
                "¿Cuál es la respuesta?",
                answers,
                2,
                Difficulty.MEDIUM
        );

        QuestionDtoResponse result = questionMapper.toDto(question);

        assertEquals("Introducción", result.intro());
        assertEquals("¿Cuál es la respuesta?", result.text());
        assertSame(answers, result.answers());
    }
}
