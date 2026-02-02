package com.jordi.wolves.wolves_api.game.model;

import com.jordi.wolves.wolves_api.game.enums.GameStatus;
import com.jordi.wolves.wolves_api.question.enums.Difficulty;
import com.jordi.wolves.wolves_api.question.model.Question;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class Game {

    @Id
    private String id;
    private String playerId;
    private Difficulty difficulty;
    private List<Question> questions;
    private int currentQuestionIndex;
    private int score;
    private GameStatus status;
    private int reward;

    public Game(String playerId,
                Difficulty difficulty,
                List<Question> questions,
                int reward)
    {

        this.playerId = playerId;
        this.difficulty = difficulty;
        this.questions = questions;
        this.currentQuestionIndex = 0;
        this.score = 0;
        this.status = GameStatus.CREATED;
        this.reward = reward;
    }

    public Game() {
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }
}
