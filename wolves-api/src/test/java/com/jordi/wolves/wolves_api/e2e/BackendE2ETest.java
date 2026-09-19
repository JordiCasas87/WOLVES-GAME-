package com.jordi.wolves.wolves_api.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordi.wolves.wolves_api.game.repository.GameRepository;
import com.jordi.wolves.wolves_api.player.repository.PlayerRepository;
import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import com.jordi.wolves.wolves_api.question.model.Question;
import com.jordi.wolves.wolves_api.question.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BackendE2ETest {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private GameRepository gameRepository;

    @BeforeEach
    void cleanDatabase() {
        restTemplate.getRestTemplate().setRequestFactory(
                new JdkClientHttpRequestFactory(HttpClient.newHttpClient()));
        gameRepository.deleteAll();
        questionRepository.deleteAll();
        playerRepository.deleteAll();
    }

    @Test
    void userRegistersAndAccessesProtectedProfile() throws Exception {
        String token = register("e2e-user");

        ResponseEntity<String> profile = restTemplate.exchange(
                "/me", HttpMethod.GET, authorizedEntity(token), String.class);

        assertThat(profile.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(profile.getBody());
        assertThat(body.get("name").asText()).isEqualTo("e2e-user");
        assertThat(body.get("role").asText()).isEqualTo("USER");
    }

    @Test
    void userUpdatesNotesAndReadsPersistedProfile() throws Exception {
        String token = register("profile-user");

        ResponseEntity<String> update = restTemplate.exchange(
                "/me/notes",
                HttpMethod.PATCH,
                authorizedEntity(token, Map.of("notes", "Review difficult questions")),
                String.class);

        assertThat(update.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> profile = restTemplate.exchange(
                "/me", HttpMethod.GET, authorizedEntity(token), String.class);
        JsonNode body = objectMapper.readTree(profile.getBody());
        assertThat(body.get("notes").asText()).isEqualTo("Review difficult questions");
    }

    @Test
    void userCompletesGameFromCreationToResult() throws Exception {
        String token = register("game-user");
        questionRepository.save(new Question(
                null,
                "Introducción",
                "¿Cuál es la respuesta correcta?",
                List.of("A", "B"),
                0,
                Difficulty.EASY));

        ResponseEntity<String> creation = restTemplate.exchange(
                "/game/new?difficulty=EASY",
                HttpMethod.POST,
                authorizedEntity(token),
                String.class);
        assertThat(creation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String gameId = objectMapper.readTree(creation.getBody()).get("id").asText();

        ResponseEntity<String> question = restTemplate.exchange(
                "/game/" + gameId + "/nextQuestion",
                HttpMethod.GET,
                authorizedEntity(token),
                String.class);
        assertThat(question.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> answer = restTemplate.exchange(
                "/game/" + gameId + "/answer",
                HttpMethod.POST,
                authorizedEntity(token, Map.of("selectedAnswer", 0)),
                String.class);
        assertThat(answer.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(answer.getBody()).get("correct").asBoolean()).isTrue();

        ResponseEntity<String> result = restTemplate.exchange(
                "/game/" + gameId + "/result",
                HttpMethod.GET,
                authorizedEntity(token),
                String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode resultBody = objectMapper.readTree(result.getBody());
        assertThat(resultBody.get("score").asInt()).isEqualTo(1);
        assertThat(resultBody.get("totalQuestions").asInt()).isEqualTo(1);
    }

    private String register(String username) throws Exception {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register",
                Map.of("name", username, "password", "secret1", "age", 30),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return objectMapper.readTree(response.getBody()).get("token").asText();
    }

    private HttpEntity<Void> authorizedEntity(String token) {
        return new HttpEntity<>(authorizedHeaders(token));
    }

    private HttpEntity<Object> authorizedEntity(String token, Object body) {
        return new HttpEntity<>(body, authorizedHeaders(token));
    }

    private HttpHeaders authorizedHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
