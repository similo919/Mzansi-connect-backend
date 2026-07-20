package com.mzansiconnect.backend.repository;

import com.mzansiconnect.backend.entity.TaxiRank;
import com.mzansiconnect.backend.enums.RankType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TaxiRankRepository
        extends JpaRepository<TaxiRank, Long>,
        JpaSpecificationExecutor<TaxiRank> {

    @EntityGraph(attributePaths = "locatedInArea")
    Optional<TaxiRank> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = "locatedInArea")
    Page<TaxiRank> findByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = "locatedInArea")
    Page<TaxiRank> findByNameContainingIgnoreCaseAndActiveTrue(
            String name,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "locatedInArea")
    Page<TaxiRank> findByLocatedInArea_IdAndActiveTrue(
            Long areaId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "locatedInArea")
    Page<TaxiRank> findByRankTypeAndActiveTrue(
            RankType rankType,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "locatedInArea")
    List<TaxiRank> findByLocatedInArea_IdAndActiveTrueOrderByNameAsc(
            Long areaId
    );

    boolean existsByNameIgnoreCaseAndLocatedInArea_Id(
            String name,
            Long areaId
    );

    boolean existsByNameIgnoreCaseAndLocatedInArea_IdAndIdNot(
            String name,
            Long areaId,
            Long excludedId
    );
}
