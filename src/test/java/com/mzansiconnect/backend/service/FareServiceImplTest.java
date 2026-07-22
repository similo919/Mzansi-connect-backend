package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.fare.FareCreateRequest;
import com.mzansiconnect.backend.dto.fare.FareResponse;
import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.entity.Fare;
import com.mzansiconnect.backend.entity.Route;
import com.mzansiconnect.backend.entity.TaxiRank;
import com.mzansiconnect.backend.enums.AreaType;
import com.mzansiconnect.backend.enums.FareType;
import com.mzansiconnect.backend.enums.RankType;
import com.mzansiconnect.backend.enums.RouteType;
import com.mzansiconnect.backend.enums.VerificationStatus;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.exception.DuplicateResourceException;
import com.mzansiconnect.backend.mapper.FareMapper;
import com.mzansiconnect.backend.repository.FareRepository;
import com.mzansiconnect.backend.repository.RouteRepository;
import com.mzansiconnect.backend.service.impl.FareServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FareServiceImplTest {

    private FareRepository fareRepository;
    private RouteRepository routeRepository;

    private FareService fareService;

    @BeforeEach
    void setUp() {
        fareRepository = mock(FareRepository.class);
        routeRepository = mock(RouteRepository.class);

        fareService = new FareServiceImpl(
                fareRepository,
                routeRepository,
                new FareMapper()
        );
    }

    @Test
    void shouldCreateValidFare() {
        Route route = createRoute();

        FareCreateRequest request =
                FareCreateRequest.builder()
                        .routeId(100L)
                        .amount(new BigDecimal("18.00"))
                        .currency("zar")
                        .fareType(FareType.STANDARD)
                        .effectiveFrom(
                                LocalDate.of(2026, 7, 1)
                        )
                        .effectiveTo(null)
                        .locallyVerified(false)
                        .verificationNotes(
                                "Initial fare"
                        )
                        .build();

        when(
                routeRepository.findByIdAndActiveTrue(100L)
        ).thenReturn(Optional.of(route));

        when(
                fareRepository.existsOverlappingFarePeriod(
                        100L,
                        FareType.STANDARD,
                        LocalDate.of(2026, 7, 1),
                        null,
                        null
                )
        ).thenReturn(false);

        when(fareRepository.save(any(Fare.class)))
                .thenAnswer(invocation -> {
                    Fare fare = invocation.getArgument(0);
                    fare.setId(200L);
                    return fare;
                });

        FareResponse result =
                fareService.createFare(request);

        assertEquals(200L, result.getId());
        assertEquals(
                new BigDecimal("18.00"),
                result.getAmount()
        );

        assertEquals("ZAR", result.getCurrency());
        assertEquals(
                FareType.STANDARD,
                result.getFareType()
        );

        assertEquals(
                "DOB-JAB-001",
                result.getRoute().getRouteCode()
        );

        assertTrue(result.getActive());

        verify(fareRepository).save(any(Fare.class));
    }

    @Test
    void shouldDefaultCurrencyToZar() {
        Route route = createRoute();

        FareCreateRequest request =
                FareCreateRequest.builder()
                        .routeId(100L)
                        .amount(new BigDecimal("18.00"))
                        .currency(null)
                        .fareType(FareType.STANDARD)
                        .effectiveFrom(
                                LocalDate.of(2026, 7, 1)
                        )
                        .locallyVerified(false)
                        .build();

        when(
                routeRepository.findByIdAndActiveTrue(100L)
        ).thenReturn(Optional.of(route));

        when(
                fareRepository.existsOverlappingFarePeriod(
                        100L,
                        FareType.STANDARD,
                        LocalDate.of(2026, 7, 1),
                        null,
                        null
                )
        ).thenReturn(false);

        when(fareRepository.save(any(Fare.class)))
                .thenAnswer(invocation -> {
                    Fare fare = invocation.getArgument(0);
                    fare.setId(200L);
                    return fare;
                });

        FareResponse result =
                fareService.createFare(request);

        assertEquals("ZAR", result.getCurrency());
    }

    @Test
    void shouldRejectEffectiveToBeforeEffectiveFrom() {
        Route route = createRoute();

        FareCreateRequest request =
                FareCreateRequest.builder()
                        .routeId(100L)
                        .amount(new BigDecimal("18.00"))
                        .currency("ZAR")
                        .fareType(FareType.STANDARD)
                        .effectiveFrom(
                                LocalDate.of(2026, 7, 10)
                        )
                        .effectiveTo(
                                LocalDate.of(2026, 7, 1)
                        )
                        .locallyVerified(false)
                        .build();

        when(
                routeRepository.findByIdAndActiveTrue(100L)
        ).thenReturn(Optional.of(route));

        assertThrows(
                BusinessValidationException.class,
                () -> fareService.createFare(request)
        );

        verify(fareRepository, never())
                .save(any(Fare.class));
    }

    @Test
    void shouldRejectOverlappingFarePeriod() {
        Route route = createRoute();

        FareCreateRequest request =
                FareCreateRequest.builder()
                        .routeId(100L)
                        .amount(new BigDecimal("20.00"))
                        .currency("ZAR")
                        .fareType(FareType.STANDARD)
                        .effectiveFrom(
                                LocalDate.of(2026, 7, 15)
                        )
                        .effectiveTo(null)
                        .locallyVerified(false)
                        .build();

        when(
                routeRepository.findByIdAndActiveTrue(100L)
        ).thenReturn(Optional.of(route));

        when(
                fareRepository.existsOverlappingFarePeriod(
                        100L,
                        FareType.STANDARD,
                        LocalDate.of(2026, 7, 15),
                        null,
                        null
                )
        ).thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> fareService.createFare(request)
        );

        verify(fareRepository, never())
                .save(any(Fare.class));
    }

    @Test
    void shouldReturnCurrentStandardFare() {
        Route route = createRoute();

        LocalDate selectedDate =
                LocalDate.of(2026, 7, 22);

        Fare fare = createFare(
                route,
                200L,
                FareType.STANDARD,
                "18.00"
        );

        when(
                routeRepository.findByIdAndActiveTrue(100L)
        ).thenReturn(Optional.of(route));

        when(
                fareRepository.findCurrentFaresByType(
                        100L,
                        FareType.STANDARD,
                        selectedDate
                )
        ).thenReturn(List.of(fare));

        List<FareResponse> results =
                fareService.getCurrentFares(
                        100L,
                        FareType.STANDARD,
                        selectedDate
                );

        assertEquals(1, results.size());

        assertEquals(
                new BigDecimal("18.00"),
                results.getFirst().getAmount()
        );

        assertEquals(
                FareType.STANDARD,
                results.getFirst().getFareType()
        );
    }

    @Test
    void shouldReturnAllCurrentFareTypes() {
        Route route = createRoute();

        LocalDate selectedDate =
                LocalDate.of(2026, 7, 22);

        Fare standardFare = createFare(
                route,
                200L,
                FareType.STANDARD,
                "18.00"
        );

        Fare weekendFare = createFare(
                route,
                201L,
                FareType.WEEKEND,
                "20.00"
        );

        when(
                routeRepository.findByIdAndActiveTrue(100L)
        ).thenReturn(Optional.of(route));

        when(
                fareRepository.findCurrentFares(
                        100L,
                        selectedDate
                )
        ).thenReturn(
                List.of(
                        standardFare,
                        weekendFare
                )
        );

        List<FareResponse> results =
                fareService.getCurrentFares(
                        100L,
                        null,
                        selectedDate
                );

        assertEquals(2, results.size());

        verify(fareRepository).findCurrentFares(
                100L,
                selectedDate
        );

        verify(fareRepository, never())
                .findCurrentFaresByType(
                        anyLong(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldDeactivateFare() {
        Route route = createRoute();

        Fare fare = createFare(
                route,
                200L,
                FareType.STANDARD,
                "18.00"
        );

        when(
                fareRepository.findByIdAndActiveTrue(200L)
        ).thenReturn(Optional.of(fare));

        fareService.deactivateFare(200L);

        assertFalse(fare.getActive());
        verify(fareRepository).save(fare);
    }

    private Route createRoute() {
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

    private Fare createFare(
            Route route,
            Long id,
            FareType fareType,
            String amount
    ) {
        return Fare.builder()
                .id(id)
                .route(route)
                .amount(new BigDecimal(amount))
                .currency("ZAR")
                .fareType(fareType)
                .effectiveFrom(
                        LocalDate.of(2026, 7, 1)
                )
                .effectiveTo(null)
                .active(true)
                .locallyVerified(false)
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
