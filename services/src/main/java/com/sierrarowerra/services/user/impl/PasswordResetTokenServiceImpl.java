package com.sierrarowerra.services.user.impl;

import com.sierrarowerra.domain.user.PasswordResetToken;
import com.sierrarowerra.domain.user.PasswordResetTokenRepository;
import com.sierrarowerra.domain.user.User;
import com.sierrarowerra.services.email.EmailService;
import com.sierrarowerra.services.user.PasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void createPasswordResetTokenForUser(User user) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        tokenRepository.save(myToken);
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByPasswordResetToken(String token) {
        return tokenRepository.findByToken(token).map(PasswordResetToken::getUser);
    }

    @Override
    @Transactional(readOnly = true)
    public void validatePasswordResetToken(String token) {
        PasswordResetToken passToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

        if (passToken.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Expired password reset token");
        }
    }

    @Override
    @Transactional
    public void deleteToken(String token) {
        tokenRepository.findByToken(token).ifPresent(tokenRepository::delete);
    }
}
