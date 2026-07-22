package com.mzansiconnect.backend.dto.rank;

import com.mzansiconnect.backend.enums.RankType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxiRankCreateRequest {

    @NotBlank(message = "Taxi rank name is required")
    @Size(
            max = 180,
            message = "Taxi rank name cannot exceed 180 characters"
    )
    private String name;

    @NotNull(message = "Taxi rank type is required")
    private RankType rankType;

    @NotNull(message = "Located area ID is required")
    @Positive(message = "Located area ID must be positive")
    private Long locatedInAreaId;

    @Size(
            max = 500,
            message = "Address description cannot exceed 500 characters"
    )
    private String addressDescription;

    @Size(
            max = 150,
            message = "Operating hours cannot exceed 150 characters"
    )
    private String operatingHours;

    @Digits(
            integer = 3,
            fraction = 7,
            message = "Latitude must have at most 3 integer digits and 7 decimal places"
    )
    @DecimalMin(
            value = "-90.0000000",
            message = "Latitude cannot be less than -90"
    )
    @DecimalMax(
            value = "90.0000000",
            message = "Latitude cannot exceed 90"
    )
    private BigDecimal latitude;

    @Digits(
            integer = 3,
            fraction = 7,
            message = "Longitude must have at most 3 integer digits and 7 decimal places"
    )
    @DecimalMin(
            value = "-180.0000000",
            message = "Longitude cannot be less than -180"
    )
    @DecimalMax(
            value = "180.0000000",
            message = "Longitude cannot exceed 180"
    )
    private BigDecimal longitude;

    private Boolean formalRank;

    private Boolean locallyVerified;

    @Size(
            max = 500,
            message = "Verification notes cannot exceed 500 characters"
    )
    private String verificationNotes;
}
