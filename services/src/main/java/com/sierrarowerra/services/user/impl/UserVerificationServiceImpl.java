package com.sierrarowerra.services.user.impl;

import com.sierrarowerra.domain.user.User;
import com.sierrarowerra.domain.user.UserRepository;
import com.sierrarowerra.services.email.EmailService;
import com.sierrarowerra.services.user.UserVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserVerificationServiceImpl implements UserVerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void createVerificationTokenForUser(User user) {
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    @Override
    @Transactional
    public boolean verifyUser(String token) {
        return userRepository.findByVerificationToken(token).map(user -> {
            user.setEnabled(true);
            user.setVerificationToken(null); // Invalidate the token after use
            userRepository.save(user);
            return true;
        }).orElse(false);
    }
}
