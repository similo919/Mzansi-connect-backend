package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.rank.TaxiRankCreateRequest;
import com.mzansiconnect.backend.dto.rank.TaxiRankResponse;
import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.entity.TaxiRank;
import com.mzansiconnect.backend.enums.AreaType;
import com.mzansiconnect.backend.enums.RankType;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.mapper.TaxiRankMapper;
import com.mzansiconnect.backend.repository.AreaRankAssignmentRepository;
import com.mzansiconnect.backend.repository.AreaRepository;
import com.mzansiconnect.backend.repository.TaxiRankRepository;
import com.mzansiconnect.backend.service.impl.TaxiRankServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaxiRankServiceImplTest {

    private TaxiRankRepository taxiRankRepository;
    private AreaRepository areaRepository;
    private AreaRankAssignmentRepository assignmentRepository;

    private TaxiRankService taxiRankService;

    @BeforeEach
    void setUp() {
        taxiRankRepository =
                mock(TaxiRankRepository.class);

        areaRepository =
                mock(AreaRepository.class);

        assignmentRepository =
                mock(AreaRankAssignmentRepository.class);

        taxiRankService = new TaxiRankServiceImpl(
                taxiRankRepository,
                areaRepository,
                assignmentRepository,
                new TaxiRankMapper()
        );
    }

    @Test
    void shouldCreateTaxiRankInSuburb() {
        Area area = Area.builder()
                .id(10L)
                .name("Dobsonville")
                .areaType(AreaType.SUBURB)
                .province("Gauteng")
                .municipality("City of Johannesburg")
                .active(true)
                .build();

        TaxiRankCreateRequest request =
                TaxiRankCreateRequest.builder()
                        .name("Test Taxi Rank")
                        .rankType(RankType.TOWNSHIP_RANK)
                        .locatedInAreaId(10L)
                        .formalRank(true)
                        .locallyVerified(false)
                        .build();

        when(areaRepository.findByIdAndActiveTrue(10L))
                .thenReturn(Optional.of(area));

        when(
                taxiRankRepository
                        .existsByNameIgnoreCaseAndLocatedInArea_Id(
                                "Test Taxi Rank",
                                10L
                        )
        ).thenReturn(false);

        when(taxiRankRepository.save(any(TaxiRank.class)))
                .thenAnswer(invocation -> {
                    TaxiRank rank = invocation.getArgument(0);
                    rank.setId(20L);
                    return rank;
                });

        TaxiRankResponse result =
                taxiRankService.createTaxiRank(request);

        assertEquals(20L, result.getId());
        assertEquals("Test Taxi Rank", result.getName());
        assertEquals(
                RankType.TOWNSHIP_RANK,
                result.getRankType()
        );
        assertEquals(
                "Dobsonville",
                result.getLocatedInArea().getName()
        );
    }

    @Test
    void shouldRejectTaxiRankAttachedToZone() {
        Area zone = Area.builder()
                .id(11L)
                .name("Diepkloof Zone 1")
                .areaType(AreaType.ZONE)
                .active(true)
                .build();

        TaxiRankCreateRequest request =
                TaxiRankCreateRequest.builder()
                        .name("Invalid Zone Rank")
                        .rankType(RankType.TOWNSHIP_RANK)
                        .locatedInAreaId(11L)
                        .build();

        when(areaRepository.findByIdAndActiveTrue(11L))
                .thenReturn(Optional.of(zone));

        assertThrows(
                BusinessValidationException.class,
                () -> taxiRankService.createTaxiRank(request)
        );

        verify(taxiRankRepository, never())
                .save(any(TaxiRank.class));
    }

    @Test
    void shouldPreventDeactivationWithActiveAssignments() {
        Area area = Area.builder()
                .id(10L)
                .name("Dobsonville")
                .areaType(AreaType.SUBURB)
                .active(true)
                .build();

        TaxiRank rank = TaxiRank.builder()
                .id(20L)
                .name("Dobsonville Taxi Rank")
                .rankType(RankType.TOWNSHIP_RANK)
                .locatedInArea(area)
                .formalRank(true)
                .active(true)
                .locallyVerified(false)
                .build();

        when(
                taxiRankRepository.findByIdAndActiveTrue(20L)
        ).thenReturn(Optional.of(rank));

        when(
                assignmentRepository
                        .existsByTaxiRank_IdAndActiveTrue(20L)
        ).thenReturn(true);

        assertThrows(
                BusinessValidationException.class,
                () -> taxiRankService.deactivateTaxiRank(20L)
        );

        verify(taxiRankRepository, never())
                .save(any(TaxiRank.class));
    }

    @Test
    void shouldDeactivateRankWithoutAssignments() {
        Area area = Area.builder()
                .id(10L)
                .name("Dobsonville")
                .areaType(AreaType.SUBURB)
                .active(true)
                .build();

        TaxiRank rank = TaxiRank.builder()
                .id(20L)
                .name("Test Rank")
                .rankType(RankType.TOWNSHIP_RANK)
                .locatedInArea(area)
                .formalRank(true)
                .active(true)
                .locallyVerified(false)
                .build();

        when(
                taxiRankRepository.findByIdAndActiveTrue(20L)
        ).thenReturn(Optional.of(rank));

        when(
                assignmentRepository
                        .existsByTaxiRank_IdAndActiveTrue(20L)
        ).thenReturn(false);

        taxiRankService.deactivateTaxiRank(20L);

        assertFalse(rank.getActive());
        verify(taxiRankRepository).save(rank);
    }
}
