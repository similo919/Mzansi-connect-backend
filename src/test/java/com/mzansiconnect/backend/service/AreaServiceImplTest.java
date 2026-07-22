package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.area.AreaCreateRequest;
import com.mzansiconnect.backend.dto.area.AreaResponse;
import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.enums.AreaType;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.exception.DuplicateResourceException;
import com.mzansiconnect.backend.mapper.AreaMapper;
import com.mzansiconnect.backend.repository.AreaRankAssignmentRepository;
import com.mzansiconnect.backend.repository.AreaRepository;
import com.mzansiconnect.backend.repository.TaxiRankRepository;
import com.mzansiconnect.backend.service.impl.AreaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AreaServiceImplTest {

    private AreaRepository areaRepository;
    private TaxiRankRepository taxiRankRepository;
    private AreaRankAssignmentRepository assignmentRepository;

    private AreaService areaService;

    @BeforeEach
    void setUp() {
        areaRepository = mock(AreaRepository.class);
        taxiRankRepository = mock(TaxiRankRepository.class);
        assignmentRepository =
                mock(AreaRankAssignmentRepository.class);

        areaService = new AreaServiceImpl(
                areaRepository,
                taxiRankRepository,
                assignmentRepository,
                new AreaMapper()
        );
    }

    @Test
    void shouldCreateSuburbUnderRegion() {
        Area soweto = Area.builder()
                .id(1L)
                .name("Soweto")
                .areaType(AreaType.REGION)
                .province("Gauteng")
                .municipality("City of Johannesburg")
                .active(true)
                .build();

        AreaCreateRequest request =
                AreaCreateRequest.builder()
                        .name("Test Township")
                        .areaType(AreaType.SUBURB)
                        .parentAreaId(1L)
                        .province("Gauteng")
                        .municipality(
                                "City of Johannesburg"
                        )
                        .description("Test area")
                        .build();

        when(areaRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(soweto));

        when(
                areaRepository
                        .existsByNameIgnoreCaseAndParentArea_Id(
                                "Test Township",
                                1L
                        )
        ).thenReturn(false);

        when(areaRepository.save(any(Area.class)))
                .thenAnswer(invocation -> {
                    Area area = invocation.getArgument(0);
                    area.setId(2L);
                    return area;
                });

        AreaResponse result =
                areaService.createArea(request);

        assertEquals(2L, result.getId());
        assertEquals("Test Township", result.getName());
        assertEquals(AreaType.SUBURB, result.getAreaType());
        assertNotNull(result.getParentArea());
        assertEquals("Soweto", result.getParentArea().getName());

        verify(areaRepository).save(any(Area.class));
    }

    @Test
    void shouldRejectDuplicateAreaUnderSameParent() {
        Area soweto = Area.builder()
                .id(1L)
                .name("Soweto")
                .areaType(AreaType.REGION)
                .active(true)
                .build();

        AreaCreateRequest request =
                AreaCreateRequest.builder()
                        .name("Diepkloof")
                        .areaType(AreaType.SUBURB)
                        .parentAreaId(1L)
                        .province("Gauteng")
                        .municipality(
                                "City of Johannesburg"
                        )
                        .build();

        when(areaRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(soweto));

        when(
                areaRepository
                        .existsByNameIgnoreCaseAndParentArea_Id(
                                "Diepkloof",
                                1L
                        )
        ).thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> areaService.createArea(request)
        );

        verify(areaRepository, never())
                .save(any(Area.class));
    }

    @Test
    void shouldRejectZoneWithoutParentTownship() {
        AreaCreateRequest request =
                AreaCreateRequest.builder()
                        .name("Test Zone 1")
                        .areaType(AreaType.ZONE)
                        .parentAreaId(null)
                        .province("Gauteng")
                        .municipality(
                                "City of Johannesburg"
                        )
                        .build();

        assertThrows(
                BusinessValidationException.class,
                () -> areaService.createArea(request)
        );

        verify(areaRepository, never())
                .save(any(Area.class));
    }

    @Test
    void shouldRejectZoneWhoseParentIsNotSuburb() {
        Area region = Area.builder()
                .id(1L)
                .name("Soweto")
                .areaType(AreaType.REGION)
                .active(true)
                .build();

        AreaCreateRequest request =
                AreaCreateRequest.builder()
                        .name("Test Zone")
                        .areaType(AreaType.ZONE)
                        .parentAreaId(1L)
                        .province("Gauteng")
                        .municipality(
                                "City of Johannesburg"
                        )
                        .build();

        when(areaRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(region));

        assertThrows(
                BusinessValidationException.class,
                () -> areaService.createArea(request)
        );
    }

    @Test
    void shouldRejectDeactivationWhenAreaHasChildren() {
        Area parent = Area.builder()
                .id(1L)
                .name("Diepkloof")
                .areaType(AreaType.SUBURB)
                .active(true)
                .build();

        Area child = Area.builder()
                .id(2L)
                .name("Diepkloof Zone 1")
                .areaType(AreaType.ZONE)
                .parentArea(parent)
                .active(true)
                .build();

        when(areaRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(parent));

        when(
                areaRepository
                        .findByParentArea_IdAndActiveTrueOrderByNameAsc(
                                1L
                        )
        ).thenReturn(List.of(child));

        assertThrows(
                BusinessValidationException.class,
                () -> areaService.deactivateArea(1L)
        );

        verify(areaRepository, never())
                .save(any(Area.class));
    }
}
