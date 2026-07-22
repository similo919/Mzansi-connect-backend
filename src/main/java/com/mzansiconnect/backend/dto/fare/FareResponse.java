package com.mzansiconnect.backend.dto.fare;

import com.mzansiconnect.backend.dto.route.RouteSummaryResponse;
import com.mzansiconnect.backend.enums.FareType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareResponse {

    private Long id;

    private RouteSummaryResponse route;

    private BigDecimal amount;
    private String currency;
    private FareType fareType;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    private Boolean active;
    private Boolean locallyVerified;

    private LocalDateTime lastVerifiedAt;
    private String verificationNotes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
