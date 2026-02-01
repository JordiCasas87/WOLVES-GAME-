package com.jordi.wolves.wolves_api.question.dto;

public record QuestionInternalDtoResponse(

        String id,
        int correctAnswerIndex) {
}
//* DTO interno para validar respuestas