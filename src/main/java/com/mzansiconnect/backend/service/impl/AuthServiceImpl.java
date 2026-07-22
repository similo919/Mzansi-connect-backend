package com.mzansiconnect.backend.service.impl;

import com.mzansiconnect.backend.dto.auth.LoginRequest;
import com.mzansiconnect.backend.dto.auth.LoginResponse;
import com.mzansiconnect.backend.dto.auth.RegisterRequest;
import com.mzansiconnect.backend.dto.auth.UserResponse;
import com.mzansiconnect.backend.entity.Role;
import com.mzansiconnect.backend.entity.User;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.exception.DuplicateResourceException;
import com.mzansiconnect.backend.exception.InvalidCredentialsException;
import com.mzansiconnect.backend.mapper.UserMapper;
import com.mzansiconnect.backend.repository.RoleRepository;
import com.mzansiconnect.backend.repository.UserRepository;
import com.mzansiconnect.backend.security.JwtService;
import com.mzansiconnect.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE =
            "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager
            authenticationManager;
    private final JwtService jwtService;

    @Override
    public UserResponse register(
            RegisterRequest request
    ) {
        String normalizedEmail =
                normalizeEmail(request.getEmail());

        validatePasswordsMatch(
                request.getPassword(),
                request.getConfirmPassword()
        );

        if (userRepository.existsByEmailIgnoreCase(
                normalizedEmail
        )) {
            throw new DuplicateResourceException(
                    "An account already exists with this email address"
            );
        }

        Role defaultRole = roleRepository
                .findByNameIgnoreCase(DEFAULT_ROLE)
                .orElseThrow(
                        () -> new BusinessValidationException(
                                "Default registration role "
                                        + DEFAULT_ROLE
                                        + " has not been configured"
                        )
                );

        User user = User.builder()
                .firstName(
                        normalizeRequiredText(
                                request.getFirstName()
                        )
                )
                .lastName(
                        normalizeRequiredText(
                                request.getLastName()
                        )
                )
                .email(normalizedEmail)
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .enabled(true)
                .build();

        user.addRole(defaultRole);

        try {
            User savedUser =
                    userRepository.saveAndFlush(user);

            return userMapper.toResponse(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException(
                    "An account already exists with this email address"
            );
        }
    }

    @Override
    public LoginResponse login(
            LoginRequest request
    ) {
        String normalizedEmail =
                normalizeEmail(request.getEmail());

        Authentication authentication;

        try {
            authentication =
                    authenticationManager.authenticate(
                            UsernamePasswordAuthenticationToken
                                    .unauthenticated(
                                            normalizedEmail,
                                            request.getPassword()
                                    )
                    );
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        User user = userRepository
                .findByEmailIgnoreCaseAndEnabledTrue(
                        normalizedEmail
                )
                .orElseThrow(
                        () -> new InvalidCredentialsException(
                                "Invalid email or password"
                        )
                );

        String accessToken =
                jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresInSeconds(
                        jwtService.getExpirationSeconds()
                )
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository
                .findByEmailIgnoreCaseAndEnabledTrue(
                        normalizeEmail(email)
                )
                .orElseThrow(
                        () -> new InvalidCredentialsException(
                                "Authenticated user was not found"
                        )
                );

        return userMapper.toResponse(user);
    }

    private void validatePasswordsMatch(
            String password,
            String confirmPassword
    ) {
        if (!password.equals(confirmPassword)) {
            throw new BusinessValidationException(
                    "Password and password confirmation do not match"
            );
        }
    }

    private String normalizeEmail(String email) {
        return email.trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeRequiredText(String value) {
        return value.trim();
    }
}
