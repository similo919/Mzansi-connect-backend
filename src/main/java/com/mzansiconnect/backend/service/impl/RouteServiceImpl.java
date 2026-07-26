package com.mzansiconnect.backend.service.impl;

import com.mzansiconnect.backend.dto.route.RouteCreateRequest;
import com.mzansiconnect.backend.dto.route.RouteResponse;
import com.mzansiconnect.backend.dto.route.RouteUpdateRequest;
import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.entity.Route;
import com.mzansiconnect.backend.entity.TaxiRank;
import com.mzansiconnect.backend.enums.RouteType;
import com.mzansiconnect.backend.enums.VerificationStatus;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.exception.DuplicateResourceException;
import com.mzansiconnect.backend.exception.ResourceNotFoundException;
import com.mzansiconnect.backend.mapper.RouteMapper;
import com.mzansiconnect.backend.repository.AreaRepository;
import com.mzansiconnect.backend.repository.FareRepository;
import com.mzansiconnect.backend.repository.RouteRepository;
import com.mzansiconnect.backend.repository.TaxiRankRepository;
import com.mzansiconnect.backend.service.RouteService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class RouteServiceImpl implements RouteService {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(
                    "id",
                    "routeCode",
                    "routeName",
                    "routeType",
                    "estimatedDurationMinutes",
                    "estimatedWaitingMinutes",
                    "verificationStatus",
                    "locallyVerified",
                    "createdAt",
                    "updatedAt"
            );

    private final RouteRepository routeRepository;
    private final FareRepository fareRepository;
    private final AreaRepository areaRepository;
    private final TaxiRankRepository taxiRankRepository;
    private final RouteMapper routeMapper;

    @Override
    public RouteResponse createRoute(
            RouteCreateRequest request
    ) {
        validateRouteRules(
                request.getStartingAreaId(),
                request.getDestinationAreaId(),
                request.getDepartureRankId(),
                request.getArrivalRankId(),
                request.getOperatesWeekdays(),
                request.getOperatesWeekends(),
                request.getVerificationStatus(),
                request.getLocallyVerified()
        );

        validateDuplicateRouteCode(
                request.getRouteCode(),
                null
        );

        Area startingArea =
                getActiveArea(request.getStartingAreaId());

        Area destinationArea =
                getActiveArea(request.getDestinationAreaId());

        TaxiRank departureRank =
                getActiveTaxiRank(request.getDepartureRankId());

        TaxiRank arrivalRank =
                getActiveTaxiRank(request.getArrivalRankId());

        Route route = routeMapper.toEntity(
                request,
                startingArea,
                destinationArea,
                departureRank,
                arrivalRank
        );

        return routeMapper.toResponse(
                routeRepository.save(route)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getRouteById(Long id) {
        return routeMapper.toResponse(
                getActiveRoute(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getRouteByCode(
            String routeCode
    ) {
        String normalizedCode =
                normalizeRouteCode(routeCode);

        Route route = routeRepository
                .findByRouteCodeIgnoreCaseAndActiveTrue(
                        normalizedCode
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Active route not found with code: "
                                        + normalizedCode
                        )
                );

        return routeMapper.toResponse(route);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RouteResponse> getRoutes(
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
    ) {
        Pageable pageable = createPageable(
                page,
                size,
                sortBy,
                sortDirection
        );

        Specification<Route> specification =
                createSpecification(
                        search,
                        startingAreaId,
                        destinationAreaId,
                        departureRankId,
                        arrivalRankId,
                        routeType,
                        verificationStatus,
                        locallyVerified,
                        operatesWeekdays,
                        operatesWeekends
                );

        if (startingAreaId != null
                && destinationAreaId != null) {
            Area requestedStartingArea =
                    getActiveArea(startingAreaId);

            Page<Route> exactRoutes =
                    routeRepository.findAll(
                            specification,
                            pageable
                    );

            if (exactRoutes.getTotalElements() > 0) {
                return exactRoutes.map(
                        route -> routeMapper.toResponse(
                                route,
                                requestedStartingArea,
                                false
                        )
                );
            }

            Page<Route> inheritedRoutes =
                    findInheritedStartingAreaRoutes(
                            requestedStartingArea,
                            search,
                            destinationAreaId,
                            departureRankId,
                            arrivalRankId,
                            routeType,
                            verificationStatus,
                            locallyVerified,
                            operatesWeekdays,
                            operatesWeekends,
                            pageable
                    );

            if (inheritedRoutes.getTotalElements() > 0) {
                return inheritedRoutes.map(
                        route -> routeMapper.toResponse(
                                route,
                                requestedStartingArea,
                                true
                        )
                );
            }

            return exactRoutes.map(
                    route -> routeMapper.toResponse(
                            route,
                            requestedStartingArea,
                            false
                    )
            );
        }

        return routeRepository
                .findAll(specification, pageable)
                .map(routeMapper::toResponse);
    }

    @Override
    public RouteResponse updateRoute(
            Long id,
            RouteUpdateRequest request
    ) {
        Route route = getActiveRoute(id);

        validateRouteRules(
                request.getStartingAreaId(),
                request.getDestinationAreaId(),
                request.getDepartureRankId(),
                request.getArrivalRankId(),
                request.getOperatesWeekdays(),
                request.getOperatesWeekends(),
                request.getVerificationStatus(),
                request.getLocallyVerified()
        );

        validateDuplicateRouteCode(
                request.getRouteCode(),
                id
        );

        Area startingArea =
                getActiveArea(request.getStartingAreaId());

        Area destinationArea =
                getActiveArea(request.getDestinationAreaId());

        TaxiRank departureRank =
                getActiveTaxiRank(request.getDepartureRankId());

        TaxiRank arrivalRank =
                getActiveTaxiRank(request.getArrivalRankId());

        routeMapper.updateEntity(
                route,
                request,
                startingArea,
                destinationArea,
                departureRank,
                arrivalRank
        );

        return routeMapper.toResponse(
                routeRepository.save(route)
        );
    }

    @Override
    public void deactivateRoute(Long id) {
        Route route = getActiveRoute(id);

        boolean hasActiveFares =
                fareRepository
                        .existsByRoute_IdAndActiveTrue(id);

        if (hasActiveFares) {
            throw new BusinessValidationException(
                    "Route cannot be deactivated while it has active fares"
            );
        }

        route.setActive(false);
        routeRepository.save(route);
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

    private Area getActiveArea(Long id) {
        return areaRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Active area not found with ID: "
                                        + id
                        )
                );
    }

    private TaxiRank getActiveTaxiRank(Long id) {
        return taxiRankRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Active taxi rank not found with ID: "
                                        + id
                        )
                );
    }

    private void validateDuplicateRouteCode(
            String routeCode,
            Long excludedRouteId
    ) {
        String normalizedCode =
                normalizeRouteCode(routeCode);

        boolean duplicate =
                excludedRouteId == null
                        ? routeRepository
                        .existsByRouteCodeIgnoreCase(
                                normalizedCode
                        )
                        : routeRepository
                        .existsByRouteCodeIgnoreCaseAndIdNot(
                                normalizedCode,
                                excludedRouteId
                        );

        if (duplicate) {
            throw new DuplicateResourceException(
                    "A route with this code already exists"
            );
        }
    }

    private void validateRouteRules(
            Long startingAreaId,
            Long destinationAreaId,
            Long departureRankId,
            Long arrivalRankId,
            Boolean operatesWeekdays,
            Boolean operatesWeekends,
            VerificationStatus verificationStatus,
            Boolean locallyVerified
    ) {
        if (startingAreaId.equals(destinationAreaId)) {
            throw new BusinessValidationException(
                    "Starting area and destination area must be different"
            );
        }

        if (departureRankId.equals(arrivalRankId)) {
            throw new BusinessValidationException(
                    "Departure rank and arrival rank must be different"
            );
        }

        if (!Boolean.TRUE.equals(operatesWeekdays)
                && !Boolean.TRUE.equals(operatesWeekends)) {

            throw new BusinessValidationException(
                    "A route must operate on weekdays, weekends or both"
            );
        }

        if (Boolean.TRUE.equals(locallyVerified)
                && verificationStatus
                == VerificationStatus.UNVERIFIED) {

            throw new BusinessValidationException(
                    "A locally verified route cannot have UNVERIFIED status"
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
                        ? "routeName"
                        : sortBy.trim();

        if (!ALLOWED_SORT_FIELDS.contains(safeSortBy)) {
            throw new BusinessValidationException(
                    "Unsupported route sort field: "
                            + safeSortBy
            );
        }

        Sort.Direction direction;

        try {
            direction = Sort.Direction.fromString(
                    sortDirection == null
                            ? "asc"
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

    private Specification<Route> createSpecification(
            String search,
            Long startingAreaId,
            Long destinationAreaId,
            Long departureRankId,
            Long arrivalRankId,
            RouteType routeType,
            VerificationStatus verificationStatus,
            Boolean locallyVerified,
            Boolean operatesWeekdays,
            Boolean operatesWeekends
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates =
                    new ArrayList<>();

            predicates.add(
                    criteriaBuilder.isTrue(
                            root.get("active")
                    )
            );

            if (search != null && !search.isBlank()) {
                String pattern =
                        "%"
                                + search.trim()
                                .toLowerCase(Locale.ROOT)
                                + "%";

                Predicate codeMatch =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("routeCode")
                                ),
                                pattern
                        );

                Predicate nameMatch =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("routeName")
                                ),
                                pattern
                        );

                Predicate signMatch =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("taxiSign")
                                ),
                                pattern
                        );

                predicates.add(
                        criteriaBuilder.or(
                                codeMatch,
                                nameMatch,
                                signMatch
                        )
                );
            }

            if (startingAreaId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("startingArea")
                                        .get("id"),
                                startingAreaId
                        )
                );
            }

            if (destinationAreaId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("destinationArea")
                                        .get("id"),
                                destinationAreaId
                        )
                );
            }

            if (departureRankId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("departureRank")
                                        .get("id"),
                                departureRankId
                        )
                );
            }

            if (arrivalRankId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("arrivalRank")
                                        .get("id"),
                                arrivalRankId
                        )
                );
            }

            if (routeType != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("routeType"),
                                routeType
                        )
                );
            }

            if (verificationStatus != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("verificationStatus"),
                                verificationStatus
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

            if (operatesWeekdays != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("operatesWeekdays"),
                                operatesWeekdays
                        )
                );
            }

            if (operatesWeekends != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("operatesWeekends"),
                                operatesWeekends
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }

    private Page<Route> findInheritedStartingAreaRoutes(
            Area requestedStartingArea,
            String search,
            Long destinationAreaId,
            Long departureRankId,
            Long arrivalRankId,
            RouteType routeType,
            VerificationStatus verificationStatus,
            Boolean locallyVerified,
            Boolean operatesWeekdays,
            Boolean operatesWeekends,
            Pageable pageable
    ) {
        Area candidateStartingArea =
                requestedStartingArea.getParentArea();

        while (candidateStartingArea != null) {
            if (Boolean.TRUE.equals(
                    candidateStartingArea.getActive()
            )) {
                Specification<Route> inheritedSpecification =
                        createSpecification(
                                search,
                                candidateStartingArea.getId(),
                                destinationAreaId,
                                departureRankId,
                                arrivalRankId,
                                routeType,
                                verificationStatus,
                                locallyVerified,
                                operatesWeekdays,
                                operatesWeekends
                        );

                Page<Route> inheritedRoutes =
                        routeRepository.findAll(
                                inheritedSpecification,
                                pageable
                        );

                if (inheritedRoutes.getTotalElements() > 0) {
                    return inheritedRoutes;
                }
            }

            candidateStartingArea =
                    candidateStartingArea.getParentArea();
        }

        return Page.empty(pageable);
    }

    private String normalizeRouteCode(String routeCode) {
        return routeCode == null
                ? null
                : routeCode.trim()
                .toUpperCase(Locale.ROOT);
    }
}
