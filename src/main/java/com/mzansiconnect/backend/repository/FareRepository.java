package com.mzansiconnect.backend.repository;

import com.mzansiconnect.backend.entity.Fare;
import com.mzansiconnect.backend.enums.FareType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FareRepository
        extends JpaRepository<Fare, Long>,
        JpaSpecificationExecutor<Fare> {

    @EntityGraph(attributePaths = "route")
    Optional<Fare> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = "route")
    Page<Fare> findByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = "route")
    Page<Fare> findByRoute_IdAndActiveTrue(
            Long routeId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "route")
    Page<Fare> findByFareTypeAndActiveTrue(
            FareType fareType,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "route")
    List<Fare> findByRoute_IdAndActiveTrueOrderByEffectiveFromDescIdDesc(
            Long routeId
    );

    boolean existsByRoute_IdAndActiveTrue(
            Long routeId
    );

    @Query("""
            SELECT fare
            FROM Fare fare
            WHERE fare.route.id = :routeId
              AND fare.active = true
              AND fare.route.active = true
              AND fare.effectiveFrom <= :onDate
              AND (
                    fare.effectiveTo IS NULL
                    OR fare.effectiveTo >= :onDate
                  )
            ORDER BY fare.effectiveFrom DESC, fare.id DESC
            """)
    @EntityGraph(attributePaths = "route")
    List<Fare> findCurrentFares(
            @Param("routeId")
            Long routeId,

            @Param("onDate")
            LocalDate onDate
    );

    @Query("""
            SELECT fare
            FROM Fare fare
            WHERE fare.route.id = :routeId
              AND fare.fareType = :fareType
              AND fare.active = true
              AND fare.route.active = true
              AND fare.effectiveFrom <= :onDate
              AND (
                    fare.effectiveTo IS NULL
                    OR fare.effectiveTo >= :onDate
                  )
            ORDER BY fare.effectiveFrom DESC, fare.id DESC
            """)
    @EntityGraph(attributePaths = "route")
    List<Fare> findCurrentFaresByType(
            @Param("routeId")
            Long routeId,

            @Param("fareType")
            FareType fareType,

            @Param("onDate")
            LocalDate onDate
    );

    @Query("""
            SELECT CASE
                       WHEN COUNT(fare) > 0
                       THEN true
                       ELSE false
                   END
            FROM Fare fare
            WHERE fare.route.id = :routeId
              AND fare.fareType = :fareType
              AND fare.active = true
              AND (
                    :excludedFareId IS NULL
                    OR fare.id <> :excludedFareId
                  )
              AND (
                    :effectiveTo IS NULL
                    OR fare.effectiveFrom <= :effectiveTo
                  )
              AND (
                    fare.effectiveTo IS NULL
                    OR fare.effectiveTo >= :effectiveFrom
                  )
            """)
    boolean existsOverlappingFarePeriod(
            @Param("routeId")
            Long routeId,

            @Param("fareType")
            FareType fareType,

            @Param("effectiveFrom")
            LocalDate effectiveFrom,

            @Param("effectiveTo")
            LocalDate effectiveTo,

            @Param("excludedFareId")
            Long excludedFareId
    );
}
