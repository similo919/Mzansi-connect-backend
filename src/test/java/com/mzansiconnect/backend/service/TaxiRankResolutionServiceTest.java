package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.rank.ResolvedTaxiRankResponse;
import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.entity.AreaRankAssignment;
import com.mzansiconnect.backend.entity.TaxiRank;
import com.mzansiconnect.backend.enums.AreaType;
import com.mzansiconnect.backend.enums.AssignmentType;
import com.mzansiconnect.backend.enums.RankType;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.exception.RankAssignmentNotFoundException;
import com.mzansiconnect.backend.mapper.TaxiRankMapper;
import com.mzansiconnect.backend.repository.AreaRankAssignmentRepository;
import com.mzansiconnect.backend.repository.AreaRepository;
import com.mzansiconnect.backend.service.impl.TaxiRankResolutionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaxiRankResolutionServiceTest {

    private AreaRepository areaRepository;

    private AreaRankAssignmentRepository
            assignmentRepository;

    private TaxiRankResolutionService service;

    @BeforeEach
    void setUp() {
        areaRepository = mock(AreaRepository.class);

        assignmentRepository =
                mock(
                        AreaRankAssignmentRepository.class
                );

        TaxiRankMapper taxiRankMapper =
                new TaxiRankMapper();

        service =
                new TaxiRankResolutionServiceImpl(
                        areaRepository,
                        assignmentRepository,
                        taxiRankMapper
                );
    }

    @Test
    void shouldResolveDirectAreaAssignment() {
        Area township = createArea(
                1L,
                "Dobsonville",
                AreaType.SUBURB,
                null
        );

        TaxiRank rank = createRank(
                10L,
                "Dobsonville Taxi Rank",
                township,
                RankType.TOWNSHIP_RANK
        );

        AreaRankAssignment assignment =
                createAssignment(
                        100L,
                        township,
                        rank,
                        AssignmentType.PRIMARY,
                        1
                );

        when(
                areaRepository.findByIdAndActiveTrue(1L)
        ).thenReturn(Optional.of(township));

        when(
                assignmentRepository
                        .findByArea_IdAndActiveTrueOrderByPriorityAscIdAsc(
                                1L
                        )
        ).thenReturn(List.of(assignment));

        ResolvedTaxiRankResponse result =
                service.resolveTaxiRank(1L);

        assertEquals(
                "Dobsonville",
                result.getSelectedArea().getName()
        );

        assertEquals(
                "Dobsonville",
                result.getMatchedArea().getName()
        );

        assertEquals(
                "Dobsonville Taxi Rank",
                result.getTaxiRank().getName()
        );

        assertFalse(result.getInheritedFromParent());
        assertEquals(
                0,
                result.getHierarchyLevelsTraversed()
        );
    }

    @Test
    void shouldInheritAssignmentFromParentTownship() {
        Area township = createArea(
                2L,
                "Diepkloof",
                AreaType.SUBURB,
                null
        );

        Area zone = createArea(
                3L,
                "Diepkloof Zone 4",
                AreaType.ZONE,
                township
        );

        TaxiRank rank = createRank(
                20L,
                "Bara Taxi Rank",
                township,
                RankType.HOSPITAL_RANK
        );

        AreaRankAssignment assignment =
                createAssignment(
                        200L,
                        township,
                        rank,
                        AssignmentType.NEAREST_MAJOR_FALLBACK,
                        1
                );

        when(
                areaRepository.findByIdAndActiveTrue(3L)
        ).thenReturn(Optional.of(zone));

        when(
                areaRepository.findByIdAndActiveTrue(2L)
        ).thenReturn(Optional.of(township));

        when(
                assignmentRepository
                        .findByArea_IdAndActiveTrueOrderByPriorityAscIdAsc(
                                3L
                        )
        ).thenReturn(List.of());

        when(
                assignmentRepository
                        .findByArea_IdAndActiveTrueOrderByPriorityAscIdAsc(
                                2L
                        )
        ).thenReturn(List.of(assignment));

        ResolvedTaxiRankResponse result =
                service.resolveTaxiRank(3L);

        assertEquals(
                "Diepkloof Zone 4",
                result.getSelectedArea().getName()
        );

        assertEquals(
                "Diepkloof",
                result.getMatchedArea().getName()
        );

        assertEquals(
                "Bara Taxi Rank",
                result.getTaxiRank().getName()
        );

        assertTrue(result.getInheritedFromParent());

        assertEquals(
                1,
                result.getHierarchyLevelsTraversed()
        );
    }

    @Test
    void shouldPreferLowerPriorityNumber() {
        Area area = createArea(
                4L,
                "Protea Glen",
                AreaType.SUBURB,
                null
        );

        TaxiRank secondaryRank = createRank(
                30L,
                "Secondary Rank",
                area,
                RankType.TOWNSHIP_RANK
        );

        TaxiRank primaryRank = createRank(
                31L,
                "Primary Rank",
                area,
                RankType.MALL_RANK
        );

        AreaRankAssignment lowerPriority =
                createAssignment(
                        300L,
                        area,
                        secondaryRank,
                        AssignmentType.SECONDARY,
                        2
                );

        AreaRankAssignment higherPriority =
                createAssignment(
                        301L,
                        area,
                        primaryRank,
                        AssignmentType.PRIMARY,
                        1
                );

        when(
                areaRepository.findByIdAndActiveTrue(4L)
        ).thenReturn(Optional.of(area));

        when(
                assignmentRepository
                        .findByArea_IdAndActiveTrueOrderByPriorityAscIdAsc(
                                4L
                        )
        ).thenReturn(
                List.of(
                        lowerPriority,
                        higherPriority
                )
        );

        ResolvedTaxiRankResponse result =
                service.resolveTaxiRank(4L);

        assertEquals(
                "Primary Rank",
                result.getTaxiRank().getName()
        );
    }

    @Test
    void shouldThrowWhenNoAssignmentExists() {
        Area area = createArea(
                5L,
                "Unknown Township",
                AreaType.SUBURB,
                null
        );

        when(
                areaRepository.findByIdAndActiveTrue(5L)
        ).thenReturn(Optional.of(area));

        when(
                assignmentRepository
                        .findByArea_IdAndActiveTrueOrderByPriorityAscIdAsc(
                                5L
                        )
        ).thenReturn(List.of());

        assertThrows(
                RankAssignmentNotFoundException.class,
                () -> service.resolveTaxiRank(5L)
        );
    }

    @Test
    void shouldRejectHierarchyLoop() {
        Area first = createArea(
                6L,
                "First Area",
                AreaType.SUBURB,
                null
        );

        Area second = createArea(
                7L,
                "Second Area",
                AreaType.SUBURB,
                first
        );

        first.setParentArea(second);

        when(
                areaRepository.findByIdAndActiveTrue(6L)
        ).thenReturn(Optional.of(first));

        when(
                areaRepository.findByIdAndActiveTrue(7L)
        ).thenReturn(Optional.of(second));

        when(
                assignmentRepository
                        .findByArea_IdAndActiveTrueOrderByPriorityAscIdAsc(
                                anyLong()
                        )
        ).thenReturn(List.of());

        assertThrows(
                BusinessValidationException.class,
                () -> service.resolveTaxiRank(6L)
        );
    }

    private Area createArea(
            Long id,
            String name,
            AreaType areaType,
            Area parentArea
    ) {
        return Area.builder()
                .id(id)
                .name(name)
                .areaType(areaType)
                .parentArea(parentArea)
                .province("Gauteng")
                .municipality(
                        "City of Johannesburg"
                )
                .active(true)
                .build();
    }

    private TaxiRank createRank(
            Long id,
            String name,
            Area locatedInArea,
            RankType rankType
    ) {
        return TaxiRank.builder()
                .id(id)
                .name(name)
                .rankType(rankType)
                .locatedInArea(locatedInArea)
                .formalRank(true)
                .active(true)
                .locallyVerified(false)
                .build();
    }

    private AreaRankAssignment createAssignment(
            Long id,
            Area area,
            TaxiRank taxiRank,
            AssignmentType assignmentType,
            Integer priority
    ) {
        return AreaRankAssignment.builder()
                .id(id)
                .area(area)
                .taxiRank(taxiRank)
                .assignmentType(assignmentType)
                .priority(priority)
                .active(true)
                .locallyVerified(false)
                .build();
    }
}
