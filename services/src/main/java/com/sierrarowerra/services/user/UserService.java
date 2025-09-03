package com.sierrarowerra.services.user;

import com.sierrarowerra.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;

public interface UserService {
    Page<User> findAll(Pageable pageable);

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    User updateUserRoles(Long id, Set<String> roles);

    void deleteUser(Long id);
}
