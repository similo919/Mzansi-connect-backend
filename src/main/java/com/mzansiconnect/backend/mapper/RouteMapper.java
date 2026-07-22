package com.mzansiconnect.backend.mapper;

import com.mzansiconnect.backend.dto.area.AreaSummaryResponse;
import com.mzansiconnect.backend.dto.rank.TaxiRankSummaryResponse;
import com.mzansiconnect.backend.dto.route.RouteCreateRequest;
import com.mzansiconnect.backend.dto.route.RouteResponse;
import com.mzansiconnect.backend.dto.route.RouteUpdateRequest;
import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.entity.Route;
import com.mzansiconnect.backend.entity.TaxiRank;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

@Component
public class RouteMapper {

    public Route toEntity(
            RouteCreateRequest request,
            Area startingArea,
            Area destinationArea,
            TaxiRank departureRank,
            TaxiRank arrivalRank
    ) {
        return Route.builder()
                .routeCode(normalizeRouteCode(request.getRouteCode()))
                .routeName(normalizeRequiredText(request.getRouteName()))
                .startingArea(startingArea)
                .destinationArea(destinationArea)
                .departureRank(departureRank)
                .arrivalRank(arrivalRank)
                .routeType(request.getRouteType())
                .taxiSign(normalizeOptionalText(request.getTaxiSign()))
                .boardingInstructions(
                        normalizeOptionalText(
                                request.getBoardingInstructions()
                        )
                )
                .dropOffInstructions(
                        normalizeOptionalText(
                                request.getDropOffInstructions()
                        )
                )
                .travelNotes(
                        normalizeOptionalText(request.getTravelNotes())
                )
                .estimatedDurationMinutes(
                        request.getEstimatedDurationMinutes()
                )
                .estimatedWaitingMinutes(
                        request.getEstimatedWaitingMinutes()
                )
                .operatesWeekdays(request.getOperatesWeekdays())
                .operatesWeekends(request.getOperatesWeekends())
                .operatingHours(
                        normalizeOptionalText(request.getOperatingHours())
                )
                .verificationStatus(request.getVerificationStatus())
                .locallyVerified(request.getLocallyVerified())
                .lastVerifiedAt(
                        Boolean.TRUE.equals(request.getLocallyVerified())
                                ? LocalDateTime.now()
                                : null
                )
                .verificationNotes(
                        normalizeOptionalText(
                                request.getVerificationNotes()
                        )
                )
                .active(true)
                .build();
    }

    public void updateEntity(
            Route route,
            RouteUpdateRequest request,
            Area startingArea,
            Area destinationArea,
            TaxiRank departureRank,
            TaxiRank arrivalRank
    ) {
        route.setRouteCode(
                normalizeRouteCode(request.getRouteCode())
        );

        route.setRouteName(
                normalizeRequiredText(request.getRouteName())
        );

        route.setStartingArea(startingArea);
        route.setDestinationArea(destinationArea);
        route.setDepartureRank(departureRank);
        route.setArrivalRank(arrivalRank);

        route.setRouteType(request.getRouteType());

        route.setTaxiSign(
                normalizeOptionalText(request.getTaxiSign())
        );

        route.setBoardingInstructions(
                normalizeOptionalText(
                        request.getBoardingInstructions()
                )
        );

        route.setDropOffInstructions(
                normalizeOptionalText(
                        request.getDropOffInstructions()
                )
        );

        route.setTravelNotes(
                normalizeOptionalText(request.getTravelNotes())
        );

        route.setEstimatedDurationMinutes(
                request.getEstimatedDurationMinutes()
        );

        route.setEstimatedWaitingMinutes(
                request.getEstimatedWaitingMinutes()
        );

        route.setOperatesWeekdays(request.getOperatesWeekdays());
        route.setOperatesWeekends(request.getOperatesWeekends());

        route.setOperatingHours(
                normalizeOptionalText(request.getOperatingHours())
        );

        route.setVerificationStatus(
                request.getVerificationStatus()
        );

        updateVerificationState(
                route,
                request.getLocallyVerified()
        );

        route.setVerificationNotes(
                normalizeOptionalText(
                        request.getVerificationNotes()
                )
        );
    }

    public RouteResponse toResponse(Route route) {
        return RouteResponse.builder()
                .id(route.getId())
                .routeCode(route.getRouteCode())
                .routeName(route.getRouteName())
                .startingArea(toAreaSummary(route.getStartingArea()))
                .destinationArea(
                        toAreaSummary(route.getDestinationArea())
                )
                .departureRank(
                        toRankSummary(route.getDepartureRank())
                )
                .arrivalRank(
                        toRankSummary(route.getArrivalRank())
                )
                .routeType(route.getRouteType())
                .taxiSign(route.getTaxiSign())
                .boardingInstructions(
                        route.getBoardingInstructions()
                )
                .dropOffInstructions(
                        route.getDropOffInstructions()
                )
                .travelNotes(route.getTravelNotes())
                .estimatedDurationMinutes(
                        route.getEstimatedDurationMinutes()
                )
                .estimatedWaitingMinutes(
                        route.getEstimatedWaitingMinutes()
                )
                .operatesWeekdays(route.getOperatesWeekdays())
                .operatesWeekends(route.getOperatesWeekends())
                .operatingHours(route.getOperatingHours())
                .verificationStatus(
                        route.getVerificationStatus()
                )
                .locallyVerified(route.getLocallyVerified())
                .lastVerifiedAt(route.getLastVerifiedAt())
                .verificationNotes(
                        route.getVerificationNotes()
                )
                .active(route.getActive())
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .build();
    }

    private AreaSummaryResponse toAreaSummary(Area area) {
        if (area == null) {
            return null;
        }

        return AreaSummaryResponse.builder()
                .id(area.getId())
                .name(area.getName())
                .areaType(area.getAreaType())
                .build();
    }

    private TaxiRankSummaryResponse toRankSummary(
            TaxiRank taxiRank
    ) {
        if (taxiRank == null) {
            return null;
        }

        return TaxiRankSummaryResponse.builder()
                .id(taxiRank.getId())
                .name(taxiRank.getName())
                .rankType(taxiRank.getRankType())
                .build();
    }

    private void updateVerificationState(
            Route route,
            Boolean locallyVerified
    ) {
        if (Boolean.TRUE.equals(locallyVerified)) {
            route.setLocallyVerified(true);

            if (route.getLastVerifiedAt() == null) {
                route.setLastVerifiedAt(LocalDateTime.now());
            }
        } else {
            route.setLocallyVerified(false);
            route.setLastVerifiedAt(null);
        }
    }

    private String normalizeRouteCode(String value) {
        return value == null
                ? null
                : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeRequiredText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
