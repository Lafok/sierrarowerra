package com.sierrarowerra.services.email.impl;

import com.sierrarowerra.services.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.oauth2.authorizedRedirectUri}")
    private String frontendBaseUrl;

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Password Reset Request");

        String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;
        message.setText("To reset your password, click the link below:\n" + resetUrl);

        mailSender.send(message);
    }
}
