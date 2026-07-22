package com.mzansiconnect.backend.dto.fare;

import com.mzansiconnect.backend.enums.FareType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareUpdateRequest {

    @NotNull(message = "Route ID is required")
    @Positive(message = "Route ID must be positive")
    private Long routeId;

    @NotNull(message = "Fare amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Fare amount must be greater than zero"
    )
    @Digits(
            integer = 8,
            fraction = 2,
            message = "Fare amount must have at most 8 integer digits and 2 decimal places"
    )
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(
            regexp = "^[A-Za-z]{3}$",
            message = "Currency must contain exactly three letters"
    )
    private String currency;

    @NotNull(message = "Fare type is required")
    private FareType fareType;

    @NotNull(message = "Effective-from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @NotNull(message = "Local verification value is required")
    private Boolean locallyVerified;

    @Size(
            max = 500,
            message = "Verification notes cannot exceed 500 characters"
    )
    private String verificationNotes;
}
