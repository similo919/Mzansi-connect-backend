package com.mzansiconnect.backend.config;

import com.mzansiconnect.backend.security.CustomUserDetailsService;
import com.mzansiconnect.backend.security.JwtAccessDeniedHandler;
import com.mzansiconnect.backend.security.JwtAuthenticationEntryPoint;
import com.mzansiconnect.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService
            userDetailsService;

    private final PasswordEncoder passwordEncoder;

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    private final JwtAuthenticationEntryPoint
            authenticationEntryPoint;

    private final JwtAccessDeniedHandler
            accessDeniedHandler;

    @Bean
    public DaoAuthenticationProvider
    authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }

    @Bean
    public AuthenticationManager
    authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration
                .getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy
                                                .STATELESS
                                )
                )

                .exceptionHandling(
                        exceptions ->
                                exceptions
                                        .authenticationEntryPoint(
                                                authenticationEntryPoint
                                        )
                                        .accessDeniedHandler(
                                                accessDeniedHandler
                                        )
                )

                .authenticationProvider(
                        authenticationProvider()
                )

                .authorizeHttpRequests(
                        authorization ->
                                authorization

                                        .requestMatchers(
                                                HttpMethod.OPTIONS,
                                                "/**"
                                        )
                                        .permitAll()

                                        .requestMatchers(
                                                "/api/auth/register",
                                                "/api/auth/login",
                                                "/api/health/**",
                                                "/error"
                                        )
                                        .permitAll()

                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/auth/me"
                                        )
                                        .authenticated()

                                        .requestMatchers(
                                                "/api/area-rank-assignments/**"
                                        )
                                        .hasAuthority("ROLE_ADMIN")

                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/api/areas/**",
                                                "/api/taxi-ranks/**",
                                                "/api/routes/**",
                                                "/api/fares/**"
                                        )
                                        .hasAuthority("ROLE_ADMIN")

                                        .requestMatchers(
                                                HttpMethod.PUT,
                                                "/api/areas/**",
                                                "/api/taxi-ranks/**",
                                                "/api/routes/**",
                                                "/api/fares/**"
                                        )
                                        .hasAuthority("ROLE_ADMIN")

                                        .requestMatchers(
                                                HttpMethod.DELETE,
                                                "/api/areas/**",
                                                "/api/taxi-ranks/**",
                                                "/api/routes/**",
                                                "/api/fares/**"
                                        )
                                        .hasAuthority("ROLE_ADMIN")

                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/areas/**",
                                                "/api/taxi-ranks/**",
                                                "/api/routes/**",
                                                "/api/fares/**"
                                        )
                                        .authenticated()

                                        .anyRequest()
                                        .denyAll()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
