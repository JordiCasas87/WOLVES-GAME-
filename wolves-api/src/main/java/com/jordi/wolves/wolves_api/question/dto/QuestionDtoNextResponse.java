package com.jordi.wolves.wolves_api.question.dto;

import java.util.List;

public record QuestionDtoNextResponse(

        int numberQuestion,
        String intro,
        String text,
        List<String> answers

) {
}
