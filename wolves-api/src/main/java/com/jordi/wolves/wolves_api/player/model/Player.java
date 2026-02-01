package com.jordi.wolves.wolves_api.player.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "players" )
public class Player {

    @Id
    private String id;
    private String name;
    private int age;
    private LocalDate dateOfCreation;
    private int level;
    private int money;
    private List<String> incorrectQuestionsIdList;

    public Player(String id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.dateOfCreation = LocalDate.now();
        this.level = 0;
        this.money = 0;
        this.incorrectQuestionsIdList = new ArrayList<>();
    }

    public Player() {
        this.incorrectQuestionsIdList = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public LocalDate getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(LocalDate dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void incrementLevel() {
        this.level++;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public void incrementMoney(int money) {
        this.money += money;
    }

    public List<String> getIncorrectQuestionsIdList() {
        return incorrectQuestionsIdList;
    }

    public void setIncorrectQuestionsIdList(List <String> incorrectQuestionsIdList) {
        this.incorrectQuestionsIdList = incorrectQuestionsIdList;
    }
}
