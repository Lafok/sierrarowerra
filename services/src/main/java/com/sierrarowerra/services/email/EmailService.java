package com.sierrarowerra.services.email;

public interface EmailService {
    void sendPasswordResetEmail(String to, String token);

    void sendVerificationEmail(String to, String token);
}
