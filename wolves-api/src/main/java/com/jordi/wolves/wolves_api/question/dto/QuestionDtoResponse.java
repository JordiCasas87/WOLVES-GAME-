package com.jordi.wolves.wolves_api.question.dto;

import java.util.List;

public record QuestionDtoResponse(

        String intro,
        String text,
        List<String> answers) {

}
