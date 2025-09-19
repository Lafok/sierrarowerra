package com.sierrarowerra.services.user;

import com.sierrarowerra.domain.user.User;

import java.util.Optional;

public interface PasswordResetTokenService {

    void createPasswordResetTokenForUser(User user);

    Optional<User> getUserByPasswordResetToken(String token);

    void validatePasswordResetToken(String token);

    void deleteToken(String token);

}
