package com.mzansiconnect.backend.repository;

import com.mzansiconnect.backend.entity.Review;
import com.mzansiconnect.backend.enums.ReviewStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    boolean existsByUser_IdAndRoute_IdAndActiveTrue(
            Long userId,
            Long routeId
    );

    @EntityGraph(attributePaths = {
            "user",
            "user.roles",
            "route",
            "route.startingArea",
            "route.destinationArea",
            "route.departureRank",
            "route.arrivalRank"
    })
    List<Review> findByRoute_IdAndStatusAndActiveTrueOrderByCreatedAtDesc(
            Long routeId,
            ReviewStatus status
    );

    @EntityGraph(attributePaths = {
            "user",
            "user.roles",
            "route",
            "route.startingArea",
            "route.destinationArea",
            "route.departureRank",
            "route.arrivalRank"
    })
    List<Review> findByUser_IdAndActiveTrueOrderByCreatedAtDesc(
            Long userId
    );

    @EntityGraph(attributePaths = {
            "user",
            "user.roles",
            "route",
            "route.startingArea",
            "route.destinationArea",
            "route.departureRank",
            "route.arrivalRank"
    })
    List<Review> findByStatusAndActiveTrueOrderByCreatedAtDesc(
            ReviewStatus status
    );
}