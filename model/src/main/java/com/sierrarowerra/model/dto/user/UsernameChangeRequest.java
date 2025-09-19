package com.sierrarowerra.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsernameChangeRequest {

    @NotBlank
    @Size(min = 3, max = 20)
    private String newUsername;
}
