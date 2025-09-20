package com.sierrarowerra.services.user;

import com.sierrarowerra.domain.user.User;

public interface UserVerificationService {
    void createVerificationTokenForUser(User user);

    boolean verifyUser(String token);
}
