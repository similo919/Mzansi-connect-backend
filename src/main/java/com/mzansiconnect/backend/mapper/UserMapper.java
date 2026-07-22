package com.mzansiconnect.backend.mapper;

import com.mzansiconnect.backend.dto.auth.UserResponse;
import com.mzansiconnect.backend.entity.Role;
import com.mzansiconnect.backend.entity.User;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        LinkedHashSet<String> roleNames =
                user.getRoles() == null
                        ? new LinkedHashSet<>()
                        : user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .sorted()
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
