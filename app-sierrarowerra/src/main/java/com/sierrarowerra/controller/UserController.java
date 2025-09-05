package com.sierrarowerra.controller;

import com.sierrarowerra.domain.user.User;
import com.sierrarowerra.model.dto.common.PageDto;
import com.sierrarowerra.model.dto.user.UserDto;
import com.sierrarowerra.model.dto.user.UserRolesRequestDto;
import com.sierrarowerra.security.services.UserDetailsImpl;
import com.sierrarowerra.services.user.UserService;
import com.sierrarowerra.services.common.mapper.PageMapper;
import com.sierrarowerra.services.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final PageMapper pageMapper;

    @Operation(summary = "Get current user's profile")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        return userService.findById(currentUser.getId())
                .map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get a paginated list of all users (Admin only)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageDto<UserDto> getAllUsers(@ParameterObject @PageableDefault(sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<User> userPage = userService.findAll(pageable);
        return pageMapper.toDto(userPage, userMapper::toDto);
    }

    @Operation(summary = "Get a paginated list of all admins (Admin only)")
    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public PageDto<UserDto> getAllAdmins(@ParameterObject @PageableDefault(sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<User> userPage = userService.findAllAdmins(pageable);
        return pageMapper.toDto(userPage, userMapper::toDto);
    }

    @Operation(summary = "Get a specific user by their ID (Admin only)")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update a user's roles (Admin only)")
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserRoles(@PathVariable Long id, @Valid @RequestBody UserRolesRequestDto rolesRequest) {
        User updatedUser = userService.updateUserRoles(id, rolesRequest.getRoles());
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }

    @Operation(summary = "Delete a user (Admin only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
