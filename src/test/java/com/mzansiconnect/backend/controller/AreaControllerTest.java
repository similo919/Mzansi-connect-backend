package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.config.SecurityConfig;
import com.mzansiconnect.backend.dto.area.AreaResponse;
import com.mzansiconnect.backend.dto.area.AreaUpdateRequest;
import com.mzansiconnect.backend.enums.AreaType;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AreaController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
class AreaControllerTest {

    private static final String VALID_UPDATE_REQUEST = """
            {
              "name": "Protea Glen",
              "areaType": "SUBURB",
              "province": "Gauteng",
              "municipality": "City of Johannesburg",
              "description": "Updated area"
            }
            """;

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
    void updateAreaShouldReturnSuccess()
            throws Exception {

        AreaResponse response =
                AreaResponse.builder()
                        .id(1L)
                        .name("Protea Glen")
                        .areaType(AreaType.SUBURB)
                        .province("Gauteng")
                        .municipality("City of Johannesburg")
                        .description("Updated area")
                        .active(true)
                        .build();

        when(
                areaService.updateArea(
                        eq(1L),
                        any(AreaUpdateRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        put("/api/areas/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_UPDATE_REQUEST)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Area updated successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data.name")
                                .value("Protea Glen")
                );
    }

    @Test
    @WithMockUser
    void updateAreaWithNonNumericIdShouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(
                        put("/api/areas/AREA_ID")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_UPDATE_REQUEST)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid value for parameter: id"
                                )
                );
    }

    @Test
    @WithMockUser
    void updateAreaWithNonPositiveIdShouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(
                        put("/api/areas/0")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_UPDATE_REQUEST)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Validation failed")
                );
    }
}
