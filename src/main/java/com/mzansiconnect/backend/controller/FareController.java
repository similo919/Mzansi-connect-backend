package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.dto.fare.FareCreateRequest;
import com.mzansiconnect.backend.dto.fare.FareResponse;
import com.mzansiconnect.backend.dto.fare.FareUpdateRequest;
import com.mzansiconnect.backend.enums.FareType;
import com.mzansiconnect.backend.response.ApiResponse;
import com.mzansiconnect.backend.service.FareService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/fares")
@RequiredArgsConstructor
@Validated
public class FareController {

    private final FareService fareService;

    @PostMapping
    public ResponseEntity<ApiResponse<FareResponse>>
    createFare(
            @Valid
            @RequestBody
            FareCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Fare created successfully",
                                fareService.createFare(request)
                        )
                );
    }

    @GetMapping
    public ResponseEntity<
            ApiResponse<Page<FareResponse>>
            > getFares(
            @RequestParam(required = false)
            @Positive(message = "Route ID must be positive")
            Long routeId,

            @RequestParam(required = false)
            FareType fareType,

            @RequestParam(required = false)
            @Pattern(
                    regexp = "^[A-Za-z]{3}$",
                    message = "Currency must contain exactly three letters"
            )
            String currency,

            @RequestParam(required = false)
            Boolean locallyVerified,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate effectiveOn,

            @RequestParam(defaultValue = "0")
            @Min(
                    value = 0,
                    message = "Page number cannot be negative"
            )
            int page,

            @RequestParam(defaultValue = "20")
            @Min(
                    value = 1,
                    message = "Page size must be at least 1"
            )
            @Max(
                    value = 100,
                    message = "Page size cannot exceed 100"
            )
            int size,

            @RequestParam(defaultValue = "effectiveFrom")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String sortDirection
    ) {
        Page<FareResponse> response =
                fareService.getFares(
                        routeId,
                        fareType,
                        currency,
                        locallyVerified,
                        effectiveOn,
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fares retrieved successfully",
                        response
                )
        );
    }

    @GetMapping("/current")
    public ResponseEntity<
            ApiResponse<List<FareResponse>>
            > getCurrentFares(
            @RequestParam
            @Positive(message = "Route ID must be positive")
            Long routeId,

            @RequestParam(required = false)
            FareType fareType,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate onDate
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current fares retrieved successfully",
                        fareService.getCurrentFares(
                                routeId,
                                fareType,
                                onDate
                        )
                )
        );
    }

    @GetMapping("/route/{routeId}/history")
    public ResponseEntity<
            ApiResponse<List<FareResponse>>
            > getRouteFareHistory(
            @PathVariable
            @Positive(message = "Route ID must be positive")
            Long routeId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Route fare history retrieved successfully",
                        fareService.getRouteFareHistory(
                                routeId
                        )
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FareResponse>>
    getFareById(
            @PathVariable
            @Positive(message = "Fare ID must be positive")
            Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fare retrieved successfully",
                        fareService.getFareById(id)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FareResponse>>
    updateFare(
            @PathVariable
            @Positive(message = "Fare ID must be positive")
            Long id,

            @Valid
            @RequestBody
            FareUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fare updated successfully",
                        fareService.updateFare(
                                id,
                                request
                        )
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>>
    deactivateFare(
            @PathVariable
            @Positive(message = "Fare ID must be positive")
            Long id
    ) {
        fareService.deactivateFare(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fare deactivated successfully"
                )
        );
    }
}
