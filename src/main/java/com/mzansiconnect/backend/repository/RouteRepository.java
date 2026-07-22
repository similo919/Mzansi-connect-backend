package com.mzansiconnect.backend.repository;

import com.mzansiconnect.backend.entity.Route;
import com.mzansiconnect.backend.enums.RouteType;
import com.mzansiconnect.backend.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface RouteRepository
        extends JpaRepository<Route, Long>,
        JpaSpecificationExecutor<Route> {

    @EntityGraph(attributePaths = {
            "startingArea",
            "destinationArea",
            "departureRank",
            "arrivalRank"
    })
    Optional<Route> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = {
            "startingArea",
            "destinationArea",
            "departureRank",
            "arrivalRank"
    })
    Optional<Route> findByRouteCodeIgnoreCaseAndActiveTrue(
            String routeCode
    );

    @EntityGraph(attributePaths = {
            "startingArea",
            "destinationArea",
            "departureRank",
            "arrivalRank"
    })
    Page<Route> findByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {
            "startingArea",
            "destinationArea",
            "departureRank",
            "arrivalRank"
    })
    Page<Route> findByStartingArea_IdAndActiveTrue(
            Long startingAreaId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "startingArea",
            "destinationArea",
            "departureRank",
            "arrivalRank"
    })
    Page<Route> findByDestinationArea_IdAndActiveTrue(
            Long destinationAreaId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "startingArea",
            "destinationArea",
            "departureRank",
            "arrivalRank"
    })
    Page<Route> findByRouteTypeAndActiveTrue(
            RouteType routeType,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "startingArea",
            "destinationArea",
            "departureRank",
            "arrivalRank"
    })
    Page<Route> findByVerificationStatusAndActiveTrue(
            VerificationStatus verificationStatus,
            Pageable pageable
    );

    boolean existsByRouteCodeIgnoreCase(
            String routeCode
    );

    boolean existsByRouteCodeIgnoreCaseAndIdNot(
            String routeCode,
            Long excludedRouteId
    );

    boolean existsByDepartureRank_IdAndActiveTrue(
            Long taxiRankId
    );

    boolean existsByArrivalRank_IdAndActiveTrue(
            Long taxiRankId
    );

    boolean existsByStartingArea_IdAndActiveTrue(
            Long areaId
    );

    boolean existsByDestinationArea_IdAndActiveTrue(
            Long areaId
    );

    @Override
    @EntityGraph(attributePaths = {
            "startingArea",
            "destinationArea",
            "departureRank",
            "arrivalRank"
    })
    Page<Route> findAll(
            Specification<Route> specification,
            Pageable pageable
    );
}
