package com.mzansiconnect.backend.mapper;

import com.mzansiconnect.backend.dto.fare.FareCreateRequest;
import com.mzansiconnect.backend.dto.fare.FareResponse;
import com.mzansiconnect.backend.dto.fare.FareUpdateRequest;
import com.mzansiconnect.backend.dto.route.RouteSummaryResponse;
import com.mzansiconnect.backend.entity.Fare;
import com.mzansiconnect.backend.entity.Route;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

@Component
public class FareMapper {

    public Fare toEntity(
            FareCreateRequest request,
            Route route
    ) {
        boolean locallyVerified =
                Boolean.TRUE.equals(
                        request.getLocallyVerified()
                );

        return Fare.builder()
                .route(route)
                .amount(request.getAmount())
                .currency(
                        normalizeCurrency(
                                request.getCurrency()
                        )
                )
                .fareType(request.getFareType())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(true)
                .locallyVerified(locallyVerified)
                .lastVerifiedAt(
                        locallyVerified
                                ? LocalDateTime.now()
                                : null
                )
                .verificationNotes(
                        normalizeOptionalText(
                                request.getVerificationNotes()
                        )
                )
                .build();
    }

    public void updateEntity(
            Fare fare,
            FareUpdateRequest request,
            Route route
    ) {
        fare.setRoute(route);
        fare.setAmount(request.getAmount());

        fare.setCurrency(
                normalizeCurrency(request.getCurrency())
        );

        fare.setFareType(request.getFareType());
        fare.setEffectiveFrom(request.getEffectiveFrom());
        fare.setEffectiveTo(request.getEffectiveTo());

        updateVerificationState(
                fare,
                request.getLocallyVerified()
        );

        fare.setVerificationNotes(
                normalizeOptionalText(
                        request.getVerificationNotes()
                )
        );
    }

    public FareResponse toResponse(Fare fare) {
        return FareResponse.builder()
                .id(fare.getId())
                .route(toRouteSummary(fare.getRoute()))
                .amount(fare.getAmount())
                .currency(fare.getCurrency())
                .fareType(fare.getFareType())
                .effectiveFrom(fare.getEffectiveFrom())
                .effectiveTo(fare.getEffectiveTo())
                .active(fare.getActive())
                .locallyVerified(fare.getLocallyVerified())
                .lastVerifiedAt(fare.getLastVerifiedAt())
                .verificationNotes(
                        fare.getVerificationNotes()
                )
                .createdAt(fare.getCreatedAt())
                .updatedAt(fare.getUpdatedAt())
                .build();
    }

    private RouteSummaryResponse toRouteSummary(
            Route route
    ) {
        if (route == null) {
            return null;
        }

        return RouteSummaryResponse.builder()
                .id(route.getId())
                .routeCode(route.getRouteCode())
                .routeName(route.getRouteName())
                .routeType(route.getRouteType())
                .build();
    }

    private void updateVerificationState(
            Fare fare,
            Boolean locallyVerified
    ) {
        if (Boolean.TRUE.equals(locallyVerified)) {
            fare.setLocallyVerified(true);

            if (fare.getLastVerifiedAt() == null) {
                fare.setLastVerifiedAt(
                        LocalDateTime.now()
                );
            }
        } else {
            fare.setLocallyVerified(false);
            fare.setLastVerifiedAt(null);
        }
    }

    private String normalizeCurrency(String value) {
        if (value == null || value.isBlank()) {
            return "ZAR";
        }

        return value.trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeOptionalText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
