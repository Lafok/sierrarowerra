package com.sierrarowerra.model.dto.user;

import lombok.Data;

import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    private String phoneNumber;
}
