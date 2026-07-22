package com.mzansiconnect.backend.service.impl;

import com.mzansiconnect.backend.dto.fare.FareCreateRequest;
import com.mzansiconnect.backend.dto.fare.FareResponse;
import com.mzansiconnect.backend.dto.fare.FareUpdateRequest;
import com.mzansiconnect.backend.entity.Fare;
import com.mzansiconnect.backend.entity.Route;
import com.mzansiconnect.backend.enums.FareType;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.exception.DuplicateResourceException;
import com.mzansiconnect.backend.exception.ResourceNotFoundException;
import com.mzansiconnect.backend.mapper.FareMapper;
import com.mzansiconnect.backend.repository.FareRepository;
import com.mzansiconnect.backend.repository.RouteRepository;
import com.mzansiconnect.backend.service.FareService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class FareServiceImpl implements FareService {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(
                    "id",
                    "amount",
                    "currency",
                    "fareType",
                    "effectiveFrom",
                    "effectiveTo",
                    "locallyVerified",
                    "lastVerifiedAt",
                    "createdAt",
                    "updatedAt"
            );

    private final FareRepository fareRepository;
    private final RouteRepository routeRepository;
    private final FareMapper fareMapper;

    @Override
    public FareResponse createFare(
            FareCreateRequest request
    ) {
        Route route =
                getActiveRoute(request.getRouteId());

        validateFarePeriod(
                request.getEffectiveFrom(),
                request.getEffectiveTo()
        );

        validateNoOverlappingFare(
                request.getRouteId(),
                request.getFareType(),
                request.getEffectiveFrom(),
                request.getEffectiveTo(),
                null
        );

        Fare fare =
                fareMapper.toEntity(request, route);

        return fareMapper.toResponse(
                fareRepository.save(fare)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FareResponse getFareById(Long id) {
        return fareMapper.toResponse(
                getActiveFare(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FareResponse> getFares(
            Long routeId,
            FareType fareType,
            String currency,
            Boolean locallyVerified,
            LocalDate effectiveOn,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Pageable pageable =
                createPageable(
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        Specification<Fare> specification =
                createSpecification(
                        routeId,
                        fareType,
                        currency,
                        locallyVerified,
                        effectiveOn
                );

        return fareRepository
                .findAll(specification, pageable)
                .map(fareMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FareResponse> getRouteFareHistory(
            Long routeId
    ) {
        getActiveRoute(routeId);

        return fareRepository
                .findByRoute_IdAndActiveTrueOrderByEffectiveFromDescIdDesc(
                        routeId
                )
                .stream()
                .map(fareMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FareResponse> getCurrentFares(
            Long routeId,
            FareType fareType,
            LocalDate onDate
    ) {
        getActiveRoute(routeId);

        LocalDate resolvedDate =
                onDate == null
                        ? LocalDate.now()
                        : onDate;

        List<Fare> fares;

        if (fareType == null) {
            fares = fareRepository.findCurrentFares(
                    routeId,
                    resolvedDate
            );
        } else {
            fares =
                    fareRepository
                            .findCurrentFaresByType(
                                    routeId,
                                    fareType,
                                    resolvedDate
                            );
        }

        return fares.stream()
                .map(fareMapper::toResponse)
                .toList();
    }

    @Override
    public FareResponse updateFare(
            Long id,
            FareUpdateRequest request
    ) {
        Fare fare = getActiveFare(id);

        Route route =
                getActiveRoute(request.getRouteId());

        validateFarePeriod(
                request.getEffectiveFrom(),
                request.getEffectiveTo()
        );

        validateNoOverlappingFare(
                request.getRouteId(),
                request.getFareType(),
                request.getEffectiveFrom(),
                request.getEffectiveTo(),
                id
        );

        fareMapper.updateEntity(
                fare,
                request,
                route
        );

        return fareMapper.toResponse(
                fareRepository.save(fare)
        );
    }

    @Override
    public void deactivateFare(Long id) {
        Fare fare = getActiveFare(id);

        fare.setActive(false);

        fareRepository.save(fare);
    }

    private Fare getActiveFare(Long id) {
        return fareRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Active fare not found with ID: "
                                        + id
                        )
                );
    }

    private Route getActiveRoute(Long id) {
        return routeRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Active route not found with ID: "
                                        + id
                        )
                );
    }

    private void validateFarePeriod(
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        if (effectiveTo != null
                && effectiveTo.isBefore(effectiveFrom)) {

            throw new BusinessValidationException(
                    "Effective-to date cannot be before the effective-from date"
            );
        }
    }

    private void validateNoOverlappingFare(
            Long routeId,
            FareType fareType,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Long excludedFareId
    ) {
        boolean overlap =
                fareRepository
                        .existsOverlappingFarePeriod(
                                routeId,
                                fareType,
                                effectiveFrom,
                                effectiveTo,
                                excludedFareId
                        );

        if (overlap) {
            throw new DuplicateResourceException(
                    "An active "
                            + fareType
                            + " fare already exists for this route during the selected period"
            );
        }
    }

    private Pageable createPageable(
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        if (page < 0) {
            throw new BusinessValidationException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessValidationException(
                    "Page size must be between 1 and "
                            + MAX_PAGE_SIZE
            );
        }

        String safeSortBy =
                sortBy == null || sortBy.isBlank()
                        ? "effectiveFrom"
                        : sortBy.trim();

        if (!ALLOWED_SORT_FIELDS.contains(safeSortBy)) {
            throw new BusinessValidationException(
                    "Unsupported fare sort field: "
                            + safeSortBy
            );
        }

        Sort.Direction direction;

        try {
            direction = Sort.Direction.fromString(
                    sortDirection == null
                            ? "desc"
                            : sortDirection
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessValidationException(
                    "Sort direction must be asc or desc"
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(direction, safeSortBy)
        );
    }

    private Specification<Fare> createSpecification(
            Long routeId,
            FareType fareType,
            String currency,
            Boolean locallyVerified,
            LocalDate effectiveOn
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates =
                    new ArrayList<>();

            predicates.add(
                    criteriaBuilder.isTrue(
                            root.get("active")
                    )
            );

            predicates.add(
                    criteriaBuilder.isTrue(
                            root.get("route")
                                    .get("active")
                    )
            );

            if (routeId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("route").get("id"),
                                routeId
                        )
                );
            }

            if (fareType != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("fareType"),
                                fareType
                        )
                );
            }

            if (currency != null
                    && !currency.isBlank()) {

                String normalizedCurrency =
                        currency.trim()
                                .toUpperCase(Locale.ROOT);

                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.upper(
                                        root.get("currency")
                                ),
                                normalizedCurrency
                        )
                );
            }

            if (locallyVerified != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("locallyVerified"),
                                locallyVerified
                        )
                );
            }

            if (effectiveOn != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("effectiveFrom"),
                                effectiveOn
                        )
                );

                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.isNull(
                                        root.get("effectiveTo")
                                ),
                                criteriaBuilder
                                        .greaterThanOrEqualTo(
                                                root.get("effectiveTo"),
                                                effectiveOn
                                        )
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(
                            new Predicate[0]
                    )
            );
        };
    }
}
