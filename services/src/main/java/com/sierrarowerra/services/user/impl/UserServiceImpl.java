package com.sierrarowerra.services.user.impl;

import com.sierrarowerra.domain.booking.BookingRepository;
import com.sierrarowerra.domain.user.RoleRepository;
import com.sierrarowerra.domain.user.UserRepository;
import com.sierrarowerra.model.enums.ERole;
import com.sierrarowerra.domain.user.Role;
import com.sierrarowerra.domain.user.User;
import com.sierrarowerra.services.exceptions.UsernameAlreadyExistsException;
import com.sierrarowerra.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    public User updateUsername(Long userId, String newUsername) {
        if (userRepository.existsByUsername(newUsername)) {
            throw new UsernameAlreadyExistsException("Error: Username is already taken!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        user.setUsername(newUsername);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUserRoles(Long id, Set<String> strRoles) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Normalize roles to lowercase to handle variations like "Admin", "ADMIN", etc.
        Set<String> normalizedRoles = strRoles == null ? new HashSet<>() :
                strRoles.stream().map(String::toLowerCase).collect(Collectors.toSet());

        boolean wasAdmin = user.getRoles().stream().anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);
        boolean willBeAdmin = normalizedRoles.contains("admin");

        if (wasAdmin && !willBeAdmin && user.isEnabled()) {
            long activeAdminCount = userRepository.countByRoles_NameAndEnabledTrue(ERole.ROLE_ADMIN);
            if (activeAdminCount <= 1) {
                throw new IllegalStateException("Cannot remove admin role from the last active administrator.");
            }
        }

        Set<Role> roles = new HashSet<>();

        if (normalizedRoles.isEmpty()) {
             Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                     .orElseThrow(() -> new RuntimeException("Error: Role 'USER' is not found."));
             roles.add(userRole);
        } else {
            normalizedRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role 'ADMIN' is not found."));
                        roles.add(adminRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role 'USER' is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // SOFT DELETE: We disable the user because physical removal breaks foreign keys 
        // in BookingHistory and PaymentHistory.
        if (!user.isEnabled()) {
             // Already disabled, nothing to do
             return;
        }

        if (user.getRoles().stream().anyMatch(role -> role.getName() == ERole.ROLE_ADMIN)) {
            long activeAdminCount = userRepository.countByRoles_NameAndEnabledTrue(ERole.ROLE_ADMIN);
            if (activeAdminCount <= 1) {
                throw new IllegalStateException("Cannot deactivate the last administrator.");
            }
        }

        if (bookingRepository.existsByUserId(id)) {
            throw new IllegalStateException("Cannot deactivate user with active bookings.");
        }
        
        user.setEnabled(false);
        userRepository.save(user);
    }
}
