package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.config.SecurityConfig;
import com.mzansiconnect.backend.exception.GlobalExceptionHandler;
import com.mzansiconnect.backend.security.CustomUserDetailsService;
import com.mzansiconnect.backend.security.JwtAuthenticationEntryPoint;
import com.mzansiconnect.backend.security.JwtService;
import com.mzansiconnect.backend.service.AreaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AreaController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
class AreaControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AreaService areaService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser
    void invalidAreaRequestShouldReturnFieldErrors()
            throws Exception {

        mockMvc.perform(
                        post("/api/areas")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "name": "",
                                          "areaType": null,
                                          "province": "",
                                          "municipality": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Validation failed")
                )
                .andExpect(
                        jsonPath("$.data.name")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.data.areaType")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.data.province")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.data.municipality")
                                .exists()
                );
    }
}
