package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.response.ApiResponse;
import com.mzansiconnect.backend.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(
            HealthService healthService
    ) {
        this.healthService = healthService;
    }

    @GetMapping
    public ResponseEntity<
            ApiResponse<Map<String, String>>
            > getApplicationHealth() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Application is running",
                        healthService.getApplicationHealth()
                )
        );
    }

    @GetMapping("/database")
    public ResponseEntity<
            ApiResponse<Map<String, String>>
            > getDatabaseHealth() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Database connection is healthy",
                        healthService.getDatabaseHealth()
                )
        );
    }
}
