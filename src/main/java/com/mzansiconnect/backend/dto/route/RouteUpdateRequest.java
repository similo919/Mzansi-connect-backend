package com.mzansiconnect.backend.dto.route;

import com.mzansiconnect.backend.enums.RouteType;
import com.mzansiconnect.backend.enums.VerificationStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteUpdateRequest {

    @NotBlank(message = "Route code is required")
    @Size(max = 60, message = "Route code cannot exceed 60 characters")
    private String routeCode;

    @NotBlank(message = "Route name is required")
    @Size(max = 200, message = "Route name cannot exceed 200 characters")
    private String routeName;

    @NotNull(message = "Starting area ID is required")
    @Positive(message = "Starting area ID must be positive")
    private Long startingAreaId;

    @NotNull(message = "Destination area ID is required")
    @Positive(message = "Destination area ID must be positive")
    private Long destinationAreaId;

    @NotNull(message = "Departure rank ID is required")
    @Positive(message = "Departure rank ID must be positive")
    private Long departureRankId;

    @NotNull(message = "Arrival rank ID is required")
    @Positive(message = "Arrival rank ID must be positive")
    private Long arrivalRankId;

    @NotNull(message = "Route type is required")
    private RouteType routeType;

    @Size(max = 150, message = "Taxi sign cannot exceed 150 characters")
    private String taxiSign;

    @Size(
            max = 1000,
            message = "Boarding instructions cannot exceed 1000 characters"
    )
    private String boardingInstructions;

    @Size(
            max = 1000,
            message = "Drop-off instructions cannot exceed 1000 characters"
    )
    private String dropOffInstructions;

    @Size(
            max = 1000,
            message = "Travel notes cannot exceed 1000 characters"
    )
    private String travelNotes;

    @NotNull(message = "Estimated duration is required")
    @Min(value = 1, message = "Estimated duration must be at least 1 minute")
    @Max(
            value = 1440,
            message = "Estimated duration cannot exceed 1440 minutes"
    )
    private Integer estimatedDurationMinutes;

    @NotNull(message = "Estimated waiting time is required")
    @Min(value = 0, message = "Estimated waiting time cannot be negative")
    @Max(
            value = 1440,
            message = "Estimated waiting time cannot exceed 1440 minutes"
    )
    private Integer estimatedWaitingMinutes;

    @NotNull(message = "Weekday operation value is required")
    private Boolean operatesWeekdays;

    @NotNull(message = "Weekend operation value is required")
    private Boolean operatesWeekends;

    @Size(
            max = 150,
            message = "Operating hours cannot exceed 150 characters"
    )
    private String operatingHours;

    @NotNull(message = "Verification status is required")
    private VerificationStatus verificationStatus;

    @NotNull(message = "Local verification value is required")
    private Boolean locallyVerified;

    @Size(
            max = 500,
            message = "Verification notes cannot exceed 500 characters"
    )
    private String verificationNotes;
}
