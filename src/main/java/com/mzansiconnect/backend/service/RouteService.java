package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.route.RouteCreateRequest;
import com.mzansiconnect.backend.dto.route.RouteResponse;
import com.mzansiconnect.backend.dto.route.RouteUpdateRequest;
import com.mzansiconnect.backend.enums.RouteType;
import com.mzansiconnect.backend.enums.VerificationStatus;
import org.springframework.data.domain.Page;

public interface RouteService {

    RouteResponse createRoute(RouteCreateRequest request);

    RouteResponse getRouteById(Long id);

    RouteResponse getRouteByCode(String routeCode);

    Page<RouteResponse> getRoutes(
            String search,
            Long startingAreaId,
            Long destinationAreaId,
            Long departureRankId,
            Long arrivalRankId,
            RouteType routeType,
            VerificationStatus verificationStatus,
            Boolean locallyVerified,
            Boolean operatesWeekdays,
            Boolean operatesWeekends,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    RouteResponse updateRoute(
            Long id,
            RouteUpdateRequest request
    );

    void deactivateRoute(Long id);
}
