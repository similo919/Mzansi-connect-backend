package com.mzansiconnect.backend.service.impl;

import com.mzansiconnect.backend.dto.auth.RegisterRequest;
import com.mzansiconnect.backend.dto.auth.UserResponse;
import com.mzansiconnect.backend.entity.Role;
import com.mzansiconnect.backend.entity.User;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.exception.DuplicateResourceException;
import com.mzansiconnect.backend.mapper.UserMapper;
import com.mzansiconnect.backend.repository.RoleRepository;
import com.mzansiconnect.backend.repository.UserRepository;
import com.mzansiconnect.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
