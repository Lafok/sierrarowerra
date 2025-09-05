package com.sierrarowerra.services.user.impl;

import com.sierrarowerra.domain.booking.BookingRepository;
import com.sierrarowerra.domain.user.RoleRepository;
import com.sierrarowerra.domain.user.UserRepository;
import com.sierrarowerra.model.enums.ERole;
import com.sierrarowerra.domain.user.Role;
import com.sierrarowerra.domain.user.User;
import com.sierrarowerra.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAllAdmins(Pageable pageable) {
        return userRepository.findAllByRoles_Name(ERole.ROLE_ADMIN, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public User updateUserRoles(Long id, Set<String> strRoles) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        Set<Role> roles = new HashSet<>();

        strRoles.forEach(role -> {
            if (role.equals("admin")) {
                Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error: Role 'ADMIN' is not found."));
                roles.add(adminRole);
            } else {
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role 'USER' is not found."));
                roles.add(userRole);
            }
        });

        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (bookingRepository.existsByUserId(id)) {
            throw new IllegalStateException("Cannot delete user with active bookings.");
        }
        userRepository.deleteById(id);
    }
}
