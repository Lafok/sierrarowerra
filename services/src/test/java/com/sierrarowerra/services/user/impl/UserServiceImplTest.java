package com.sierrarowerra.services.user.impl;

import com.sierrarowerra.domain.booking.BookingRepository;
import com.sierrarowerra.domain.user.Role;
import com.sierrarowerra.domain.user.RoleRepository;
import com.sierrarowerra.domain.user.User;
import com.sierrarowerra.domain.user.UserRepository;
import com.sierrarowerra.model.enums.ERole;
import com.sierrarowerra.services.exceptions.UsernameAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User adminUser;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        adminRole = new Role(1, ERole.ROLE_ADMIN);
        userRole = new Role(2, ERole.ROLE_USER);

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setEnabled(true);
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        adminUser.setRoles(roles);
    }

    @Test
    void updateUserRoles_ShouldUpdateRoles_WhenValid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        User updatedUser = userService.updateUserRoles(1L, Set.of("admin"));

        assertNotNull(updatedUser);
        verify(userRepository).save(adminUser);
    }

    @Test
    void updateUserRoles_ShouldThrowException_WhenRemovingLastAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRoles_NameAndEnabledTrue(ERole.ROLE_ADMIN)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> 
            userService.updateUserRoles(1L, Set.of("user"))
        );
    }

    @Test
    void deleteUser_ShouldDisableUser_WhenNotLastAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRoles_NameAndEnabledTrue(ERole.ROLE_ADMIN)).thenReturn(2L);
        when(bookingRepository.existsByUserId(1L)).thenReturn(false);

        userService.deleteUser(1L);

        assertFalse(adminUser.isEnabled());
        verify(userRepository).save(adminUser);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserHasActiveBookings() {
        adminUser.setRoles(new HashSet<>(Set.of(userRole))); // Make him a normal user for this test
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(bookingRepository.existsByUserId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void updateUsername_ShouldThrowException_WhenUsernameTaken() {
        when(userRepository.existsByUsername("newname")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> 
            userService.updateUsername(1L, "newname")
        );
    }
}
