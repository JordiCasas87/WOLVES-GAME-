package com.jordi.wolves.wolves_api.question.controller;

import com.jordi.wolves.wolves_api.question.dto.QuestionAdminListDto;
import com.jordi.wolves.wolves_api.question.dto.QuestionDtoResponse;
import com.jordi.wolves.wolves_api.question.exception.QuestionNotFoundException;
import com.jordi.wolves.wolves_api.question.service.QuestionService;
import com.jordi.wolves.wolves_api.security.jwt.JwtAuthenticationFilter;
import com.jordi.wolves.wolves_api.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionService questionService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @Test
    void getRandomQuestionsReturnsQuestionList() throws Exception {
        when(questionService.getRandomQuestions()).thenReturn(List.of(
                new QuestionDtoResponse(
                        "Primera introducción",
                        "Primera pregunta",
                        List.of("A", "B", "C", "D")
                ),
                new QuestionDtoResponse(
                        "Segunda introducción",
                        "Segunda pregunta",
                        List.of("A", "B", "C", "D")
                )
        ));

        mockMvc.perform(get("/questions/random"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].intro").value("Primera introducción"))
                .andExpect(jsonPath("$[0].text").value("Primera pregunta"))
                .andExpect(jsonPath("$[0].answers.length()").value(4))
                .andExpect(jsonPath("$[1].text").value("Segunda pregunta"));

        verify(questionService).getRandomQuestions();
    }

    @Test
    void deleteQuestionReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/questions/question-1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(questionService).deleteQuestionById("question-1");
    }

    @Test
    void deleteQuestionReturnsNotFoundWhenQuestionDoesNotExist() throws Exception {
        org.mockito.Mockito.doThrow(new QuestionNotFoundException("Question not found"))
                .when(questionService)
                .deleteQuestionById("missing-question");

        mockMvc.perform(delete("/questions/missing-question"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Question not found"));
    }

    @Test
    void getAllQuestionsReturnsAdministrativeList() throws Exception {
        when(questionService.getAllQuestionsForAdmin()).thenReturn(List.of(
                new QuestionAdminListDto("question-1", "Primera pregunta"),
                new QuestionAdminListDto("question-2", "Segunda pregunta")
        ));

        mockMvc.perform(get("/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("question-1"))
                .andExpect(jsonPath("$[0].text").value("Primera pregunta"))
                .andExpect(jsonPath("$[1].id").value("question-2"));

        verify(questionService).getAllQuestionsForAdmin();
    }
}
