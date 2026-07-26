package com.mzansiconnect.backend.dto.route;

import com.mzansiconnect.backend.dto.area.AreaSummaryResponse;
import com.mzansiconnect.backend.dto.rank.TaxiRankSummaryResponse;
import com.mzansiconnect.backend.enums.RouteType;
import com.mzansiconnect.backend.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {

    private Long id;
    private String routeCode;
    private String routeName;

    private AreaSummaryResponse startingArea;
    private AreaSummaryResponse destinationArea;
    private AreaSummaryResponse requestedStartingArea;
    private AreaSummaryResponse routeStartingArea;
    private Boolean inheritedFromParent;

    private Long requestedStartingAreaId;
    private String requestedStartingAreaName;
    private Long actualRouteStartingAreaId;
    private String actualRouteStartingAreaName;
    private Boolean parentFallbackUsed;

    private TaxiRankSummaryResponse departureRank;
    private TaxiRankSummaryResponse arrivalRank;

    private RouteType routeType;

    private String taxiSign;
    private String boardingInstructions;
    private String dropOffInstructions;
    private String travelNotes;

    private Integer estimatedDurationMinutes;
    private Integer estimatedWaitingMinutes;

    private Boolean operatesWeekdays;
    private Boolean operatesWeekends;

    private String operatingHours;

    private VerificationStatus verificationStatus;
    private Boolean locallyVerified;
    private LocalDateTime lastVerifiedAt;
    private String verificationNotes;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
