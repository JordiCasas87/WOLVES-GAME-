package com.jordi.wolves.wolves_api.question.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import com.jordi.wolves.wolves_api.question.dto.QuestionAdminListDto;
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

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);
    private QuestionRepository questionRepository;
    private QuestionMapper questionMapper;
    private static final int QUESTIONS_PER_GAME = 10;

    public QuestionService(QuestionRepository questionRepository, QuestionMapper questionMapper) {
        this.questionRepository = questionRepository;
        this.questionMapper = questionMapper;
    }

    public List<QuestionDtoResponse> getRandomQuestions() {
        // para informar de que pedimos preguntas
        log.info("Solicitando preguntas aleatorias para una nueva partida");
        List<Question> questions = questionRepository.findAll();

        Collections.shuffle(questions);
        // preguntas entregadas.
        log.info("Devolviendo {} preguntas aleatorias", QUESTIONS_PER_GAME);
        return questions.stream()
                .limit(QUESTIONS_PER_GAME)
                .map(questionMapper::toDto)
                .toList();
    }

    public List<Question> getRandomQuestionEntitiesByDifficulty(Difficulty difficulty) {
        // igual que en el primer metodo
        log.info("Solicitando preguntas aleatorias con dificultad {}", difficulty);
        List<Question> questions = questionRepository.findByDifficulty(difficulty);
        log.debug("Preguntas encontradas para dificultad {}: {}", difficulty, questions.size());
        Collections.shuffle(questions);
        return questions.stream()
                .limit(QUESTIONS_PER_GAME)
                .toList();
    }

    public List<Question> getRandomQuestionsByIds(List<String> ids, int amount) {
        log.info("Solicitando {} preguntas por IDs", amount);
        List<Question> questions = questionRepository.findByIdIn(ids);
        log.debug("Preguntas encontradas por IDs: {}", questions.size());
        Collections.shuffle(questions);
        return questions.stream()
                .limit(amount)
                .toList();
    }

    //cache evict aquí
    @CacheEvict(value = "questions_admin", allEntries = true)
    public void deleteQuestionById(String id) {
        // opcion de admin, info de eliminar pregunta.
        log.info("Intentando eliminar pregunta con id {}", id);
        if (!questionRepository.existsById(id)) {
            log.error("No se puede eliminar la pregunta. No existe una pregunta con id {}", id);
            throw new QuestionNotFoundException("Question not found with id: " + id);
        }
        questionRepository.deleteById(id);
        // ok a eliminar
        log.info("Pregunta eliminada correctamente con id {}", id);
    }

    // son muchos dato que no cambiaran, cachear
    @Cacheable("questions_admin")
    public List<QuestionAdminListDto> getAllQuestionsForAdmin() {
        log.info("Solicitando listado completo de preguntas para administrador");
        log.debug("Obteniendo listado de preguntas para admin desde base de datos (no caché)");
        List<QuestionAdminListDto> result = questionRepository.findAll()
                .stream()
                .map(q -> new QuestionAdminListDto(
                        q.getId(),
                        q.getText()
                ))
                .toList();
        return result;
    }

}
