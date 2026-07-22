package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.fare.FareCreateRequest;
import com.mzansiconnect.backend.dto.fare.FareResponse;
import com.mzansiconnect.backend.dto.fare.FareUpdateRequest;
import com.mzansiconnect.backend.enums.FareType;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface FareService {

    FareResponse createFare(FareCreateRequest request);

    FareResponse getFareById(Long id);

    Page<FareResponse> getFares(
            Long routeId,
            FareType fareType,
            String currency,
            Boolean locallyVerified,
            LocalDate effectiveOn,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    List<FareResponse> getRouteFareHistory(
            Long routeId
    );

    List<FareResponse> getCurrentFares(
            Long routeId,
            FareType fareType,
            LocalDate onDate
    );

    FareResponse updateFare(
            Long id,
            FareUpdateRequest request
    );

    void deactivateFare(Long id);
}
