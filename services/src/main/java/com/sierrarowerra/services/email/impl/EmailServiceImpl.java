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

    @Value("${app.frontend.base-url}") // Using a more generic base URL
    private String frontendBaseUrl;

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;
        String text = "To reset your password, click the link below:\n" + resetUrl;
        sendEmail(to, "Password Reset Request", text);
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = frontendBaseUrl + "/verify-email?token=" + token;
        String text = "To verify your email address, click the link below:\n" + verificationUrl;
        sendEmail(to, "Email Verification", text);
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
