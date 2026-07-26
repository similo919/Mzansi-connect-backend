package com.mzansiconnect.backend.repository;

import com.mzansiconnect.backend.entity.SavedRoute;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedRouteRepository
        extends JpaRepository<SavedRoute, Long> {

    boolean existsByUser_IdAndRoute_Id(
            Long userId,
            Long routeId
    );

    @EntityGraph(attributePaths = {
            "route",
            "route.startingArea",
            "route.destinationArea",
            "route.departureRank",
            "route.arrivalRank"
    })
    List<SavedRoute> findByUser_IdOrderByCreatedAtDesc(
            Long userId
    );

    Optional<SavedRoute> findByUser_IdAndRoute_Id(
            Long userId,
            Long routeId
    );
}