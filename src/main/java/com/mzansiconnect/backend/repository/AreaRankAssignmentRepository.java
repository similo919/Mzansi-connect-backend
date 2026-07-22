package com.mzansiconnect.backend.repository;

import com.mzansiconnect.backend.entity.AreaRankAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AreaRankAssignmentRepository
        extends JpaRepository<AreaRankAssignment, Long>,
        JpaSpecificationExecutor<AreaRankAssignment> {

    @EntityGraph(attributePaths = {
            "area",
            "area.parentArea",
            "taxiRank",
            "taxiRank.locatedInArea"
    })
    Optional<AreaRankAssignment> findByIdAndActiveTrue(
            Long id
    );

    @EntityGraph(attributePaths = {
            "area",
            "area.parentArea",
            "taxiRank",
            "taxiRank.locatedInArea"
    })
    Page<AreaRankAssignment> findByActiveTrue(
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "area",
            "area.parentArea",
            "taxiRank",
            "taxiRank.locatedInArea"
    })
    Page<AreaRankAssignment> findByArea_IdAndActiveTrue(
            Long areaId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "area",
            "area.parentArea",
            "taxiRank",
            "taxiRank.locatedInArea"
    })
    Page<AreaRankAssignment> findByTaxiRank_IdAndActiveTrue(
            Long taxiRankId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "area",
            "area.parentArea",
            "taxiRank",
            "taxiRank.locatedInArea"
    })
    List<AreaRankAssignment>
    findByArea_IdAndActiveTrueOrderByPriorityAscIdAsc(
            Long areaId
    );

    boolean existsByArea_IdAndTaxiRank_Id(
            Long areaId,
            Long taxiRankId
    );

    boolean existsByArea_IdAndTaxiRank_IdAndIdNot(
            Long areaId,
            Long taxiRankId,
            Long excludedId
    );

    boolean existsByTaxiRank_IdAndActiveTrue(
            Long taxiRankId
    );
}
