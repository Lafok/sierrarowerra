package com.sierrarowerra.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    @Schema(description = "Username or email of the user", example = "user@example.com")
    private String login;

    @NotBlank
    @Schema(description = "User password", example = "password123")
    private String password;
}
