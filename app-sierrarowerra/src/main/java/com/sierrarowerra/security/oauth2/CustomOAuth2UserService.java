package com.sierrarowerra.security.oauth2;

import com.sierrarowerra.domain.user.*;
import com.sierrarowerra.model.enums.ERole;
import com.sierrarowerra.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends OidcUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        try {
            return processOidcUser(userRequest, oidcUser);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OidcUser processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser) {
        String email = oidcUser.getAttribute("email");
        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getProvider().equals(AuthProvider.local)) {
                // This is our key scenario: linking a local account
                if (!user.isEnabled()) {
                    throw new OAuth2AuthenticationProcessingException("Your account is not activated. Please check your email for the verification link.");
                }
                // The local account is verified, so we can safely link it.
                user.setProvider(AuthProvider.google);
                user = userRepository.save(user);
            }
            // If provider is already google, we do nothing.

        } else {
            // If user is not found, register a new one.
            user = registerNewUser(oidcUser);
        }

        return UserDetailsImpl.build(user, oidcUser);
    }

    private User registerNewUser(OidcUser oidcUser) {
        User user = new User();

        user.setProvider(AuthProvider.google);
        user.setEmail(oidcUser.getEmail());

        // Since the user is registering via Google, we trust the email is verified.
        user.setEnabled(true);

        String username = generateUsername(oidcUser.getEmail());
        user.setUsername(username);

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER is not found."));
        user.setRoles(Set.of(userRole));

        return userRepository.save(user);
    }

    private String generateUsername(String email) {
        String baseUsername = email.substring(0, email.indexOf("@"));
        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        return username;
    }
}
