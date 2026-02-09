package com.jordi.wolves.wolves_api.player.dto;

import com.jordi.wolves.wolves_api.player.enums.Role;

public record PlayerAdminUpdateDto(
        String name,
        Integer age,
        Integer level,
        Role role,
        Integer money

) {
}
