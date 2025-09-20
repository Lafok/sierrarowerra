package com.sierrarowerra.controller;

import com.sierrarowerra.domain.user.AuthProvider;
import com.sierrarowerra.domain.user.Role;
import com.sierrarowerra.domain.user.RoleRepository;
import com.sierrarowerra.domain.user.User;
import com.sierrarowerra.domain.user.UserRepository;
import com.sierrarowerra.model.dto.auth.*;
import com.sierrarowerra.model.dto.common.MessageResponse;
import com.sierrarowerra.model.dto.user.PasswordChangeRequest;
import com.sierrarowerra.model.enums.ERole;
import com.sierrarowerra.security.jwt.JwtUtils;
import com.sierrarowerra.security.services.UserDetailsImpl;
import com.sierrarowerra.services.user.PasswordResetTokenService;
import com.sierrarowerra.services.user.UserVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    PasswordResetTokenService passwordResetTokenService;

    @Autowired
    UserVerificationService userVerificationService;

    @Operation(summary = "Authenticate user and get JWT token")
    @PostMapping("/signin")
    @SecurityRequirements
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        String login = loginRequest.getLogin();

        User user = userRepository.findByUsername(login)
                .or(() -> userRepository.findByEmail(login))
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with login: " + login));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @Operation(summary = "Register a new user and send verification email")
    @PostMapping("/signup")
    @SecurityRequirements
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account (it will be disabled by default)
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        user.setPhone(signUpRequest.getPhone());
        user.setProvider(AuthProvider.local);

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER is not found."));
        user.setRoles(Set.of(userRole));

        // Create verification token and send email
        userVerificationService.createVerificationTokenForUser(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully! Please check your email to verify your account."));
    }

    @Operation(summary = "Verify user's email using a token")
    @GetMapping("/verify-email")
    @SecurityRequirements
    public ResponseEntity<?> verifyUser(@RequestParam("token") String token) {
        boolean isVerified = userVerificationService.verifyUser(token);

        if (isVerified) {
            return ResponseEntity.ok(new MessageResponse("Email verified successfully! You can now log in."));
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid or expired verification token."));
        }
    }

    @Operation(summary = "Request a password reset")
    @PostMapping("/forgot-password")
    @SecurityRequirements
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        Optional<User> userOptional = userRepository.findByEmail(forgotPasswordRequest.getEmail());

        userOptional.ifPresent(passwordResetTokenService::createPasswordResetTokenForUser);

        return ResponseEntity.ok(new MessageResponse("If an account with that email exists, a password reset link has been sent."));
    }

    @Operation(summary = "Reset password using a token")
    @PostMapping("/reset-password")
    @SecurityRequirements
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        String token = resetPasswordRequest.getToken();
        passwordResetTokenService.validatePasswordResetToken(token);

        Optional<User> userOptional = passwordResetTokenService.getUserByPasswordResetToken(token);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid token!"));
        }

        User user = userOptional.get();
        user.setPassword(encoder.encode(resetPasswordRequest.getNewPassword()));
        userRepository.save(user);

        passwordResetTokenService.deleteToken(token);

        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully!"));
    }

    @Operation(summary = "Change user password")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        if (!encoder.matches(passwordChangeRequest.getOldPassword(), user.getPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Incorrect old password!"));
        }

        user.setPassword(encoder.encode(passwordChangeRequest.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Password changed successfully!"));
    }
}
