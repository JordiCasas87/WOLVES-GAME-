package com.jordi.wolves.wolves_api.question.mapper;

import com.jordi.wolves.wolves_api.question.dto.QuestionDtoResponse;
import com.jordi.wolves.wolves_api.question.model.Question;
import org.springframework.stereotype.Component;


@Component
public class QuestionMapper {

    public QuestionDtoResponse toDto(Question question) {
        return new QuestionDtoResponse(
                question.getIntro(),
                question.getText(),
                question.getAnswers()
        );
    }

    public Question toEntity(QuestionDtoResponse questionDtoResponse) {

        return  new Question(null, questionDtoResponse.intro(),
                questionDtoResponse.text(),
                questionDtoResponse.answers(),
                -1,
                null);
    }

}
