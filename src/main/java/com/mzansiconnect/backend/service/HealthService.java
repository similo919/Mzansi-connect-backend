package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.exception.DatabaseConnectionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class HealthService {

    private final JdbcTemplate jdbcTemplate;
    private final String applicationName;

    public HealthService(
            JdbcTemplate jdbcTemplate,
            @Value(
                "${spring.application.name:mzansi-connect-backend}"
            )
            String applicationName
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.applicationName = applicationName;
    }

    public Map<String, String> getApplicationHealth() {
        Map<String, String> health =
                new LinkedHashMap<>();

        health.put("application", applicationName);
        health.put("status", "UP");

        return health;
    }

    public Map<String, String> getDatabaseHealth() {
        try {
            String databaseName =
                    jdbcTemplate.queryForObject(
                            "SELECT DATABASE()",
                            String.class
                    );

            if (databaseName == null
                    || databaseName.isBlank()) {

                throw new DatabaseConnectionException(
                        "No active database was selected"
                );
            }

            Map<String, String> health =
                    new LinkedHashMap<>();

            health.put("status", "CONNECTED");
            health.put("database", databaseName);

            return health;

        } catch (DataAccessException exception) {
            throw new DatabaseConnectionException(
                    "Database connection check failed",
                    exception
            );
        }
    }
}
