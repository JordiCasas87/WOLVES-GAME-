package com.jordi.wolves.wolves_api.player.model;

import com.jordi.wolves.wolves_api.player.enums.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Document(collection = "players" )
public class Player implements UserDetails {

    @Id
    private String id;
    private String name;
    private String password;
    private Role role;
    private int age;
    private LocalDate dateOfCreation;
    private int level;
    private int gamesPlayed;
    private int money;
    private List<String> incorrectQuestionsIdList;
    // Removed constructor with (String id, String name, String password, Role role, int age)

    public Player(String name, String password, Role role, int age) {
        this.name = name;
        this.password = password;
        this.role = role;
        this.age = age;
        this.dateOfCreation = LocalDate.now();
        this.level = 0;
        this.gamesPlayed = 0;
        this.money = 0;
        this.incorrectQuestionsIdList = new ArrayList<>();
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void incrementGamesPlayed () {
        this.gamesPlayed++;
    }

    public Player() {
        this.dateOfCreation = LocalDate.now();
        this.level = 0;
        this.gamesPlayed = 0;
        this.money = 0;
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

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    //metodos añadidos por la interfaz UserDetails


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
