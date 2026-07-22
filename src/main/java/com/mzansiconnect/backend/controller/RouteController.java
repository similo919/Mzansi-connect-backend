package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.dto.route.RouteCreateRequest;
import com.mzansiconnect.backend.dto.route.RouteResponse;
import com.mzansiconnect.backend.dto.route.RouteUpdateRequest;
import com.mzansiconnect.backend.enums.RouteType;
import com.mzansiconnect.backend.enums.VerificationStatus;
import com.mzansiconnect.backend.response.ApiResponse;
import com.mzansiconnect.backend.service.RouteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Validated
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    public ResponseEntity<ApiResponse<RouteResponse>>
    createRoute(
            @Valid
            @RequestBody
            RouteCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Route created successfully",
                                routeService.createRoute(request)
                        )
                );
    }

    @GetMapping
    public ResponseEntity<
            ApiResponse<Page<RouteResponse>>
            > getRoutes(
            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            @Positive(message = "Starting area ID must be positive")
            Long startingAreaId,

            @RequestParam(required = false)
            @Positive(message = "Destination area ID must be positive")
            Long destinationAreaId,

            @RequestParam(required = false)
            @Positive(message = "Departure rank ID must be positive")
            Long departureRankId,

            @RequestParam(required = false)
            @Positive(message = "Arrival rank ID must be positive")
            Long arrivalRankId,

            @RequestParam(required = false)
            RouteType routeType,

            @RequestParam(required = false)
            VerificationStatus verificationStatus,

            @RequestParam(required = false)
            Boolean locallyVerified,

            @RequestParam(required = false)
            Boolean operatesWeekdays,

            @RequestParam(required = false)
            Boolean operatesWeekends,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number cannot be negative")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size cannot exceed 100")
            int size,

            @RequestParam(defaultValue = "routeName")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String sortDirection
    ) {
        Page<RouteResponse> response =
                routeService.getRoutes(
                        search,
                        startingAreaId,
                        destinationAreaId,
                        departureRankId,
                        arrivalRankId,
                        routeType,
                        verificationStatus,
                        locallyVerified,
                        operatesWeekdays,
                        operatesWeekends,
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Routes retrieved successfully",
                        response
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>>
    getRouteById(
            @PathVariable
            @Positive(message = "Route ID must be positive")
            Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Route retrieved successfully",
                        routeService.getRouteById(id)
                )
        );
    }

    @GetMapping("/code/{routeCode}")
    public ResponseEntity<ApiResponse<RouteResponse>>
    getRouteByCode(
            @PathVariable
            String routeCode
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Route retrieved successfully",
                        routeService.getRouteByCode(routeCode)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>>
    updateRoute(
            @PathVariable
            @Positive(message = "Route ID must be positive")
            Long id,

            @Valid
            @RequestBody
            RouteUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Route updated successfully",
                        routeService.updateRoute(id, request)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>>
    deactivateRoute(
            @PathVariable
            @Positive(message = "Route ID must be positive")
            Long id
    ) {
        routeService.deactivateRoute(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Route deactivated successfully"
                )
        );
    }
}
