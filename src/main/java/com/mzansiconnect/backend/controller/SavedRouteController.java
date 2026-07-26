package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.dto.savedroute.SavedRouteResponse;
import com.mzansiconnect.backend.response.ApiResponse;
import com.mzansiconnect.backend.service.SavedRouteService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/saved-routes")
@RequiredArgsConstructor
@Validated
public class SavedRouteController {

    private final SavedRouteService savedRouteService;

    @PostMapping("/{routeId}")
    public ResponseEntity<ApiResponse<SavedRouteResponse>> saveRoute(
            @PathVariable
            @Positive(message = "Route ID must be positive")
            Long routeId,

            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Route saved successfully",
                                savedRouteService.saveRoute(
                                        authentication.getName(),
                                        routeId
                                )
                        )
                );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavedRouteResponse>>>
    getSavedRoutes(Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Saved routes retrieved successfully",
                        savedRouteService.getSavedRoutes(
                                authentication.getName()
                        )
                )
        );
    }

    @DeleteMapping("/{routeId}")
    public ResponseEntity<ApiResponse<Void>> deleteSavedRoute(
            @PathVariable
            @Positive(message = "Route ID must be positive")
            Long routeId,

            Authentication authentication
    ) {
        savedRouteService.deleteSavedRoute(
                authentication.getName(),
                routeId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Saved route removed successfully"
                )
        );
    }
}