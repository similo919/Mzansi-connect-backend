package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.route.RouteCreateRequest;
import com.mzansiconnect.backend.dto.route.RouteResponse;
import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.entity.Route;
import com.mzansiconnect.backend.entity.TaxiRank;
import com.mzansiconnect.backend.enums.AreaType;
import com.mzansiconnect.backend.enums.RankType;
import com.mzansiconnect.backend.enums.RouteType;
import com.mzansiconnect.backend.enums.VerificationStatus;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.exception.DuplicateResourceException;
import com.mzansiconnect.backend.mapper.RouteMapper;
import com.mzansiconnect.backend.repository.AreaRepository;
import com.mzansiconnect.backend.repository.FareRepository;
import com.mzansiconnect.backend.repository.RouteRepository;
import com.mzansiconnect.backend.repository.TaxiRankRepository;
import com.mzansiconnect.backend.service.impl.RouteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RouteServiceImplTest {

    private RouteRepository routeRepository;
    private FareRepository fareRepository;
    private AreaRepository areaRepository;
    private TaxiRankRepository taxiRankRepository;

    private RouteService routeService;

    @BeforeEach
    void setUp() {
        routeRepository = mock(RouteRepository.class);
        fareRepository = mock(FareRepository.class);
        areaRepository = mock(AreaRepository.class);
        taxiRankRepository = mock(TaxiRankRepository.class);

        routeService = new RouteServiceImpl(
                routeRepository,
                fareRepository,
                areaRepository,
                taxiRankRepository,
                new RouteMapper()
        );
    }

    @Test
    void shouldCreateValidRoute() {
        Area startingArea = createArea(
                1L,
                "Dobsonville"
        );

        Area destinationArea = createArea(
                2L,
                "Jabulani"
        );

        TaxiRank departureRank = createRank(
                10L,
                "Dobsonville Taxi Rank",
                startingArea
        );

        TaxiRank arrivalRank = createRank(
                20L,
                "Jabulani Mall Taxi Rank",
                destinationArea
        );

        RouteCreateRequest request = validRequest();

        when(
                routeRepository.existsByRouteCodeIgnoreCase(
                        "DOB-JAB-001"
                )
        ).thenReturn(false);

        when(
                areaRepository.findByIdAndActiveTrue(1L)
        ).thenReturn(Optional.of(startingArea));

        when(
                areaRepository.findByIdAndActiveTrue(2L)
        ).thenReturn(Optional.of(destinationArea));

        when(
                taxiRankRepository.findByIdAndActiveTrue(10L)
        ).thenReturn(Optional.of(departureRank));

        when(
                taxiRankRepository.findByIdAndActiveTrue(20L)
        ).thenReturn(Optional.of(arrivalRank));

        when(routeRepository.save(any(Route.class)))
                .thenAnswer(invocation -> {
                    Route route = invocation.getArgument(0);
                    route.setId(100L);
                    return route;
                });

        RouteResponse result =
                routeService.createRoute(request);

        assertEquals(100L, result.getId());
        assertEquals("DOB-JAB-001", result.getRouteCode());
        assertEquals(
                "Dobsonville to Jabulani",
                result.getRouteName()
        );

        assertEquals(
                "Dobsonville",
                result.getStartingArea().getName()
        );

        assertEquals(
                "Jabulani",
                result.getDestinationArea().getName()
        );

        assertEquals(
                "Dobsonville Taxi Rank",
                result.getDepartureRank().getName()
        );

        assertEquals(
                "Jabulani Mall Taxi Rank",
                result.getArrivalRank().getName()
        );

        assertTrue(result.getActive());

        verify(routeRepository).save(any(Route.class));
    }

    @Test
    void shouldNormalizeRouteCodeToUppercase() {
        RouteCreateRequest request = validRequest();
        request.setRouteCode(" dob-jab-001 ");

        Area startingArea = createArea(
                1L,
                "Dobsonville"
        );

        Area destinationArea = createArea(
                2L,
                "Jabulani"
        );

        TaxiRank departureRank = createRank(
                10L,
                "Dobsonville Taxi Rank",
                startingArea
        );

        TaxiRank arrivalRank = createRank(
                20L,
                "Jabulani Mall Taxi Rank",
                destinationArea
        );

        when(
                routeRepository.existsByRouteCodeIgnoreCase(
                        "DOB-JAB-001"
                )
        ).thenReturn(false);

        when(
                areaRepository.findByIdAndActiveTrue(1L)
        ).thenReturn(Optional.of(startingArea));

        when(
                areaRepository.findByIdAndActiveTrue(2L)
        ).thenReturn(Optional.of(destinationArea));

        when(
                taxiRankRepository.findByIdAndActiveTrue(10L)
        ).thenReturn(Optional.of(departureRank));

        when(
                taxiRankRepository.findByIdAndActiveTrue(20L)
        ).thenReturn(Optional.of(arrivalRank));

        when(routeRepository.save(any(Route.class)))
                .thenAnswer(invocation -> {
                    Route route = invocation.getArgument(0);
                    route.setId(100L);
                    return route;
                });

        RouteResponse result =
                routeService.createRoute(request);

        assertEquals(
                "DOB-JAB-001",
                result.getRouteCode()
        );
    }

    @Test
    void shouldRejectDuplicateRouteCode() {
        RouteCreateRequest request = validRequest();

        when(
                routeRepository.existsByRouteCodeIgnoreCase(
                        "DOB-JAB-001"
                )
        ).thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> routeService.createRoute(request)
        );

        verify(routeRepository, never())
                .save(any(Route.class));
    }

    @Test
    void shouldRejectSameStartingAndDestinationArea() {
        RouteCreateRequest request = validRequest();

        request.setDestinationAreaId(
                request.getStartingAreaId()
        );

        assertThrows(
                BusinessValidationException.class,
                () -> routeService.createRoute(request)
        );

        verify(routeRepository, never())
                .save(any(Route.class));
    }

    @Test
    void shouldRejectSameDepartureAndArrivalRank() {
        RouteCreateRequest request = validRequest();

        request.setArrivalRankId(
                request.getDepartureRankId()
        );

        assertThrows(
                BusinessValidationException.class,
                () -> routeService.createRoute(request)
        );

        verify(routeRepository, never())
                .save(any(Route.class));
    }

    @Test
    void shouldRejectRouteThatNeverOperates() {
        RouteCreateRequest request = validRequest();

        request.setOperatesWeekdays(false);
        request.setOperatesWeekends(false);

        assertThrows(
                BusinessValidationException.class,
                () -> routeService.createRoute(request)
        );

        verify(routeRepository, never())
                .save(any(Route.class));
    }

    @Test
    void shouldRejectLocallyVerifiedRouteWithUnverifiedStatus() {
        RouteCreateRequest request = validRequest();

        request.setLocallyVerified(true);
        request.setVerificationStatus(
                VerificationStatus.UNVERIFIED
        );

        assertThrows(
                BusinessValidationException.class,
                () -> routeService.createRoute(request)
        );

        verify(routeRepository, never())
                .save(any(Route.class));
    }

    @Test
    void shouldPreventDeactivationWhenRouteHasActiveFares() {
        Route route = createRouteEntity();

        when(
                routeRepository.findByIdAndActiveTrue(100L)
        ).thenReturn(Optional.of(route));

        when(
                fareRepository.existsByRoute_IdAndActiveTrue(100L)
        ).thenReturn(true);

        assertThrows(
                BusinessValidationException.class,
                () -> routeService.deactivateRoute(100L)
        );

        verify(routeRepository, never()).save(route);
        assertTrue(route.getActive());
    }

    @Test
    void shouldReturnParentRouteWhenRequestedStartingAreaHasNoRoute() {
        Area diepkloof = createArea(
                6L,
                "Diepkloof"
        );

        Area diepkloofZoneOne = createArea(
                42L,
                "Diepkloof Zone 1"
        );
        diepkloofZoneOne.setParentArea(diepkloof);

        Area johannesburgCbd = createArea(
                67L,
                "Johannesburg CBD"
        );

        Route inheritedRoute = Route.builder()
                .id(200L)
                .routeCode("DEV-C-6-67")
                .routeName("Diepkloof to Johannesburg CBD")
                .startingArea(diepkloof)
                .destinationArea(johannesburgCbd)
                .departureRank(
                        createRank(
                                30L,
                                "Diepkloof Taxi Rank",
                                diepkloof
                        )
                )
                .arrivalRank(
                        createRank(
                                40L,
                                "Johannesburg CBD Taxi Rank",
                                johannesburgCbd
                        )
                )
                .routeType(RouteType.CONNECTING)
                .estimatedDurationMinutes(35)
                .estimatedWaitingMinutes(10)
                .operatesWeekdays(true)
                .operatesWeekends(true)
                .verificationStatus(
                        VerificationStatus.UNVERIFIED
                )
                .locallyVerified(false)
                .active(true)
                .build();

        when(
                areaRepository.findByIdAndActiveTrue(42L)
        ).thenReturn(Optional.of(diepkloofZoneOne));

        when(
                routeRepository.findAll(
                        org.mockito.ArgumentMatchers
                                .<Specification<Route>>any(),
                        any(Pageable.class)
                )
        ).thenReturn(
                Page.empty(),
                new PageImpl<>(List.of(inheritedRoute))
        );

        Page<RouteResponse> result =
                routeService.getRoutes(
                        null,
                        42L,
                        67L,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        20,
                        "routeName",
                        "asc"
                );

        RouteResponse response =
                result.getContent().getFirst();

        assertEquals("DEV-C-6-67", response.getRouteCode());
        assertEquals(
                "Diepkloof Zone 1",
                response.getRequestedStartingArea().getName()
        );
        assertEquals(
                "Diepkloof",
                response.getRouteStartingArea().getName()
        );
        assertTrue(response.getInheritedFromParent());
    }

    @Test
    void shouldDeactivateRouteWithoutActiveFares() {
        Route route = createRouteEntity();

        when(
                routeRepository.findByIdAndActiveTrue(100L)
        ).thenReturn(Optional.of(route));

        when(
                fareRepository.existsByRoute_IdAndActiveTrue(100L)
        ).thenReturn(false);

        routeService.deactivateRoute(100L);

        assertFalse(route.getActive());
        verify(routeRepository).save(route);
    }

    private RouteCreateRequest validRequest() {
        return RouteCreateRequest.builder()
                .routeCode("DOB-JAB-001")
                .routeName("Dobsonville to Jabulani")
                .startingAreaId(1L)
                .destinationAreaId(2L)
                .departureRankId(10L)
                .arrivalRankId(20L)
                .routeType(RouteType.DIRECT)
                .taxiSign("Jabulani")
                .boardingInstructions(
                        "Board at the main taxi queue"
                )
                .dropOffInstructions(
                        "Exit at Jabulani Mall"
                )
                .travelNotes(
                        "Confirm the destination with the driver"
                )
                .estimatedDurationMinutes(25)
                .estimatedWaitingMinutes(15)
                .operatesWeekdays(true)
                .operatesWeekends(true)
                .operatingHours("05:00 - 21:00")
                .verificationStatus(
                        VerificationStatus.UNVERIFIED
                )
                .locallyVerified(false)
                .verificationNotes(
                        "Initial route entry"
                )
                .build();
    }

    private Route createRouteEntity() {
        Area startingArea = createArea(
                1L,
                "Dobsonville"
        );

        Area destinationArea = createArea(
                2L,
                "Jabulani"
        );

        return Route.builder()
                .id(100L)
                .routeCode("DOB-JAB-001")
                .routeName("Dobsonville to Jabulani")
                .startingArea(startingArea)
                .destinationArea(destinationArea)
                .departureRank(
                        createRank(
                                10L,
                                "Dobsonville Taxi Rank",
                                startingArea
                        )
                )
                .arrivalRank(
                        createRank(
                                20L,
                                "Jabulani Mall Taxi Rank",
                                destinationArea
                        )
                )
                .routeType(RouteType.DIRECT)
                .estimatedDurationMinutes(25)
                .estimatedWaitingMinutes(15)
                .operatesWeekdays(true)
                .operatesWeekends(true)
                .verificationStatus(
                        VerificationStatus.UNVERIFIED
                )
                .locallyVerified(false)
                .active(true)
                .build();
    }

    private Area createArea(
            Long id,
            String name
    ) {
        return Area.builder()
                .id(id)
                .name(name)
                .areaType(AreaType.SUBURB)
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
            Area area
    ) {
        return TaxiRank.builder()
                .id(id)
                .name(name)
                .rankType(RankType.TOWNSHIP_RANK)
                .locatedInArea(area)
                .formalRank(true)
                .active(true)
                .locallyVerified(false)
                .build();
    }

}
