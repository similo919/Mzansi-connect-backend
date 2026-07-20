package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.exception.DatabaseConnectionException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthServiceTest {

    private final JdbcTemplate jdbcTemplate =
            mock(JdbcTemplate.class);

    private final HealthService healthService =
            new HealthService(
                    jdbcTemplate,
                    "mzansi-connect-backend"
            );

    @Test
    void applicationHealthShouldReturnUpStatus() {
        Map<String, String> result =
                healthService.getApplicationHealth();

        assertEquals(
                "mzansi-connect-backend",
                result.get("application")
        );

        assertEquals(
                "UP",
                result.get("status")
        );
    }

    @Test
    void databaseHealthShouldReturnDatabaseName() {
        when(
                jdbcTemplate.queryForObject(
                        "SELECT DATABASE()",
                        String.class
                )
        ).thenReturn(
                "taxi_route_planner_db"
        );

        Map<String, String> result =
                healthService.getDatabaseHealth();

        assertEquals(
                "CONNECTED",
                result.get("status")
        );

        assertEquals(
                "taxi_route_planner_db",
                result.get("database")
        );
    }

    @Test
    void databaseHealthShouldThrowSafeException() {
        when(
                jdbcTemplate.queryForObject(
                        "SELECT DATABASE()",
                        String.class
                )
        ).thenThrow(
                new DataAccessResourceFailureException(
                        "Access denied for internal database"
                )
        );

        DatabaseConnectionException exception =
                assertThrows(
                        DatabaseConnectionException.class,
                        healthService::getDatabaseHealth
                );

        assertEquals(
                "Database connection check failed",
                exception.getMessage()
        );
    }
}
