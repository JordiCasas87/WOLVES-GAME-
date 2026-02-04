package com.jordi.wolves.wolves_api.player.dto;

import com.jordi.wolves.wolves_api.player.enums.Role;

import java.time.LocalDate;
import java.util.List;

public record PlayerDtoResponse (

        String id,
        String name,
        int age,
        LocalDate dateOfCreation,
        int level,
        int money,
        List<String> incorrectQuestionsIdList,
        Role role

) {
}
