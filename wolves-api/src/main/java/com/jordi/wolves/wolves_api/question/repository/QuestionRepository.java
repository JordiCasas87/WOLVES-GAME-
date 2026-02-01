package com.jordi.wolves.wolves_api.question.repository;

import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import com.jordi.wolves.wolves_api.question.model.Question;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuestionRepository extends MongoRepository<Question,String> {

    List<Question> findByDifficulty(Difficulty difficulty);


}
