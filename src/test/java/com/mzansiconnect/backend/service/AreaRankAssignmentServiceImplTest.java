package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.assignment.AreaRankAssignmentCreateRequest;
import com.mzansiconnect.backend.dto.assignment.AreaRankAssignmentResponse;
import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.entity.AreaRankAssignment;
import com.mzansiconnect.backend.entity.TaxiRank;
import com.mzansiconnect.backend.enums.AreaType;
import com.mzansiconnect.backend.enums.AssignmentType;
import com.mzansiconnect.backend.enums.RankType;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.mapper.AreaRankAssignmentMapper;
import com.mzansiconnect.backend.repository.AreaRankAssignmentRepository;
import com.mzansiconnect.backend.repository.AreaRepository;
import com.mzansiconnect.backend.repository.TaxiRankRepository;
import com.mzansiconnect.backend.service.impl.AreaRankAssignmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AreaRankAssignmentServiceImplTest {

    private AreaRankAssignmentRepository assignmentRepository;
    private AreaRepository areaRepository;
    private TaxiRankRepository taxiRankRepository;

    private AreaRankAssignmentService assignmentService;

    @BeforeEach
    void setUp() {
        assignmentRepository =
                mock(AreaRankAssignmentRepository.class);

        areaRepository =
                mock(AreaRepository.class);

        taxiRankRepository =
                mock(TaxiRankRepository.class);

        assignmentService =
                new AreaRankAssignmentServiceImpl(
                        assignmentRepository,
                        areaRepository,
                        taxiRankRepository,
                        new AreaRankAssignmentMapper()
                );
    }

    @Test
    void shouldCreateMallFallbackAssignment() {
        Area area = createArea(
                1L,
                "Orlando West",
                AreaType.SUBURB,
                null
        );

        TaxiRank mallRank = createRank(
                10L,
                "Maponya Mall Taxi Rank",
                area,
                RankType.MALL_RANK
        );

        AreaRankAssignmentCreateRequest request =
                AreaRankAssignmentCreateRequest.builder()
                        .areaId(1L)
                        .taxiRankId(10L)
                        .assignmentType(
                                AssignmentType
                                        .NEAREST_MALL_FALLBACK
                        )
                        .priority(1)
                        .assignmentReason(
                                "Nearest confirmed mall rank"
                        )
                        .locallyVerified(false)
                        .build();

        when(areaRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(area));

        when(
                taxiRankRepository.findByIdAndActiveTrue(10L)
        ).thenReturn(Optional.of(mallRank));

        when(
                assignmentRepository
                        .existsByArea_IdAndTaxiRank_IdAndActiveTrue(
                                1L,
                                10L
                        )
        ).thenReturn(false);

        when(
                assignmentRepository
                        .save(any(AreaRankAssignment.class))
        ).thenAnswer(invocation -> {
            AreaRankAssignment assignment =
                    invocation.getArgument(0);

            assignment.setId(100L);
            return assignment;
        });

        AreaRankAssignmentResponse result =
                assignmentService.createAssignment(request);

        assertEquals(100L, result.getId());
        assertEquals(
                AssignmentType.NEAREST_MALL_FALLBACK,
                result.getAssignmentType()
        );
        assertEquals(
                "Maponya Mall Taxi Rank",
                result.getTaxiRank().getName()
        );
    }

    @Test
    void shouldRejectMallFallbackUsingNonMallRank() {
        Area area = createArea(
                1L,
                "Orlando West",
                AreaType.SUBURB,
                null
        );

        TaxiRank townshipRank = createRank(
                10L,
                "Township Rank",
                area,
                RankType.TOWNSHIP_RANK
        );

        AreaRankAssignmentCreateRequest request =
                AreaRankAssignmentCreateRequest.builder()
                        .areaId(1L)
                        .taxiRankId(10L)
                        .assignmentType(
                                AssignmentType
                                        .NEAREST_MALL_FALLBACK
                        )
                        .priority(1)
                        .assignmentReason(
                                "Fallback rank"
                        )
                        .build();

        when(areaRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(area));

        when(
                taxiRankRepository.findByIdAndActiveTrue(10L)
        ).thenReturn(Optional.of(townshipRank));

        when(
                assignmentRepository
                        .existsByArea_IdAndTaxiRank_IdAndActiveTrue(
                                1L,
                                10L
                        )
        ).thenReturn(false);

        assertThrows(
                BusinessValidationException.class,
                () -> assignmentService
                        .createAssignment(request)
        );

        verify(assignmentRepository, never())
                .save(any(AreaRankAssignment.class));
    }

    @Test
    void shouldCreateInheritedAssignmentWhenParentUsesRank() {
        Area township = createArea(
                1L,
                "Diepkloof",
                AreaType.SUBURB,
                null
        );

        Area zone = createArea(
                2L,
                "Diepkloof Zone 1",
                AreaType.ZONE,
                township
        );

        TaxiRank rank = createRank(
                10L,
                "Bara Taxi Rank",
                township,
                RankType.HOSPITAL_RANK
        );

        AreaRankAssignmentCreateRequest request =
                AreaRankAssignmentCreateRequest.builder()
                        .areaId(2L)
                        .taxiRankId(10L)
                        .assignmentType(
                                AssignmentType.INHERITED
                        )
                        .priority(1)
                        .assignmentReason(
                                "Inherited from Diepkloof"
                        )
                        .build();

        when(areaRepository.findByIdAndActiveTrue(2L))
                .thenReturn(Optional.of(zone));

        when(
                taxiRankRepository.findByIdAndActiveTrue(10L)
        ).thenReturn(Optional.of(rank));

        when(
                assignmentRepository
                        .existsByArea_IdAndTaxiRank_IdAndActiveTrue(
                                2L,
                                10L
                        )
        ).thenReturn(false);

        when(
                assignmentRepository
                        .existsByArea_IdAndTaxiRank_IdAndActiveTrue(
                                1L,
                                10L
                        )
        ).thenReturn(true);

        when(
                assignmentRepository
                        .save(any(AreaRankAssignment.class))
        ).thenAnswer(invocation -> {
            AreaRankAssignment assignment =
                    invocation.getArgument(0);

            assignment.setId(101L);
            return assignment;
        });

        AreaRankAssignmentResponse result =
                assignmentService.createAssignment(request);

        assertEquals(
                AssignmentType.INHERITED,
                result.getAssignmentType()
        );

        assertEquals(
                "Diepkloof Zone 1",
                result.getArea().getName()
        );
    }

    @Test
    void shouldRejectInheritedAssignmentWhenParentDoesNotUseRank() {
        Area township = createArea(
                1L,
                "Diepkloof",
                AreaType.SUBURB,
                null
        );

        Area zone = createArea(
                2L,
                "Diepkloof Zone 1",
                AreaType.ZONE,
                township
        );

        TaxiRank rank = createRank(
                10L,
                "Bara Taxi Rank",
                township,
                RankType.HOSPITAL_RANK
        );

        AreaRankAssignmentCreateRequest request =
                AreaRankAssignmentCreateRequest.builder()
                        .areaId(2L)
                        .taxiRankId(10L)
                        .assignmentType(
                                AssignmentType.INHERITED
                        )
                        .priority(1)
                        .assignmentReason(
                                "Inherited from parent"
                        )
                        .build();

        when(areaRepository.findByIdAndActiveTrue(2L))
                .thenReturn(Optional.of(zone));

        when(
                taxiRankRepository.findByIdAndActiveTrue(10L)
        ).thenReturn(Optional.of(rank));

        when(
                assignmentRepository
                        .existsByArea_IdAndTaxiRank_IdAndActiveTrue(
                                2L,
                                10L
                        )
        ).thenReturn(false);

        when(
                assignmentRepository
                        .existsByArea_IdAndTaxiRank_IdAndActiveTrue(
                                1L,
                                10L
                        )
        ).thenReturn(false);

        assertThrows(
                BusinessValidationException.class,
                () -> assignmentService
                        .createAssignment(request)
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
            Area area,
            RankType rankType
    ) {
        return TaxiRank.builder()
                .id(id)
                .name(name)
                .rankType(rankType)
                .locatedInArea(area)
                .formalRank(true)
                .active(true)
                .locallyVerified(false)
                .build();
    }
}
