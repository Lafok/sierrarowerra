package com.sierrarowerra.model.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UserRolesRequestDto {
    @NotEmpty
    private Set<String> roles;
}
