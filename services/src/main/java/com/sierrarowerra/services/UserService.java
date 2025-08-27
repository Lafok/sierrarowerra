package com.sierrarowerra.services;

import com.sierrarowerra.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {
    Page<User> findAll(Pageable pageable);

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);
}
