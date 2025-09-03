package com.sierrarowerra.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    @Schema(description = "Username for the new account", example = "newuser")
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    @Schema(description = "Email for the new account", example = "newuser@example.com")
    private String email;

    @NotBlank
    @Size(min = 10, max = 15)
    @Schema(description = "Phone number for the new account", example = "1234567890")
    private String phone;

    @Schema(description = "Set of roles for the new user. If not specified, 'ROLE_USER' will be assigned.", example = "[\"user\"]")
    private Set<String> role;

    @NotBlank
    @Size(min = 6, max = 40)
    @Schema(description = "Password for the new account", example = "password123")
    private String password;
}
