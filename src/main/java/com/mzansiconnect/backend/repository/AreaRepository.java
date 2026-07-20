package com.mzansiconnect.backend.repository;

import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.enums.AreaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AreaRepository
        extends JpaRepository<Area, Long>,
        JpaSpecificationExecutor<Area> {

    @EntityGraph(attributePaths = "parentArea")
    Optional<Area> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = "parentArea")
    Page<Area> findByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = "parentArea")
    Page<Area> findByNameContainingIgnoreCaseAndActiveTrue(
            String name,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "parentArea")
    Page<Area> findByAreaTypeAndActiveTrue(
            AreaType areaType,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "parentArea")
    List<Area> findByParentArea_IdAndActiveTrueOrderByNameAsc(
            Long parentAreaId
    );

    boolean existsByNameIgnoreCaseAndParentAreaIsNull(
            String name
    );

    boolean existsByNameIgnoreCaseAndParentArea_Id(
            String name,
            Long parentAreaId
    );

    boolean existsByNameIgnoreCaseAndParentAreaIsNullAndIdNot(
            String name,
            Long excludedId
    );

    boolean existsByNameIgnoreCaseAndParentArea_IdAndIdNot(
            String name,
            Long parentAreaId,
            Long excludedId
    );
}
