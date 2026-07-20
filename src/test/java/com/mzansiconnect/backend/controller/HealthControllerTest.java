package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.config.SecurityConfig;
import com.mzansiconnect.backend.exception.DatabaseConnectionException;
import com.mzansiconnect.backend.exception.GlobalExceptionHandler;
import com.mzansiconnect.backend.service.HealthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthService healthService;

    @Test
    void applicationHealthShouldReturnSuccess()
            throws Exception {

        Map<String, String> health =
                new LinkedHashMap<>();

        health.put(
                "application",
                "mzansi-connect-backend"
        );
        health.put("status", "UP");

        when(
                healthService.getApplicationHealth()
        ).thenReturn(health);

        mockMvc.perform(
                        get("/api/health")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                    "Application is running"
                                )
                )
                .andExpect(
                        jsonPath("$.data.application")
                                .value(
                                    "mzansi-connect-backend"
                                )
                )
                .andExpect(
                        jsonPath("$.data.status")
                                .value("UP")
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                );
    }

    @Test
    void databaseHealthShouldReturnSuccess()
            throws Exception {

        Map<String, String> health =
                new LinkedHashMap<>();

        health.put("status", "CONNECTED");
        health.put(
                "database",
                "taxi_route_planner_db"
        );

        when(
                healthService.getDatabaseHealth()
        ).thenReturn(health);

        mockMvc.perform(
                        get("/api/health/database")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data.status")
                                .value("CONNECTED")
                )
                .andExpect(
                        jsonPath("$.data.database")
                                .value(
                                    "taxi_route_planner_db"
                                )
                );
    }

    @Test
    void databaseHealthShouldReturnServiceUnavailable()
            throws Exception {

        when(
                healthService.getDatabaseHealth()
        ).thenThrow(
                new DatabaseConnectionException(
                        "Connection failed"
                )
        );

        mockMvc.perform(
                        get("/api/health/database")
                )
                .andExpect(
                        status().isServiceUnavailable()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                    "The database is currently unavailable"
                                )
                )
                .andExpect(
                        jsonPath("$.data")
                                .doesNotExist()
                );
    }
}
