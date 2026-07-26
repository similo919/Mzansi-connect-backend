package com.mzansiconnect.backend.service.impl;

import com.mzansiconnect.backend.dto.savedroute.SavedRouteResponse;
import com.mzansiconnect.backend.entity.Route;
import com.mzansiconnect.backend.entity.SavedRoute;
import com.mzansiconnect.backend.entity.User;
import com.mzansiconnect.backend.exception.DuplicateResourceException;
import com.mzansiconnect.backend.exception.ResourceNotFoundException;
import com.mzansiconnect.backend.mapper.RouteMapper;
import com.mzansiconnect.backend.repository.RouteRepository;
import com.mzansiconnect.backend.repository.SavedRouteRepository;
import com.mzansiconnect.backend.repository.UserRepository;
import com.mzansiconnect.backend.service.SavedRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SavedRouteServiceImpl implements SavedRouteService {

    private final SavedRouteRepository savedRouteRepository;
    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final RouteMapper routeMapper;

    @Override
    public SavedRouteResponse saveRoute(
            String email,
            Long routeId
    ) {
        User user = getCurrentUser(email);
        Route route = getActiveRoute(routeId);

        if (savedRouteRepository.existsByUser_IdAndRoute_Id(
                user.getId(),
                route.getId()
        )) {
            throw new DuplicateResourceException(
                    "This route is already saved"
            );
        }

        SavedRoute savedRoute = SavedRoute.builder()
                .user(user)
                .route(route)
                .build();

        return toResponse(savedRouteRepository.save(savedRoute));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedRouteResponse> getSavedRoutes(String email) {
        User user = getCurrentUser(email);

        return savedRouteRepository
                .findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteSavedRoute(
            String email,
            Long routeId
    ) {
        User user = getCurrentUser(email);

        SavedRoute savedRoute = savedRouteRepository
                .findByUser_IdAndRoute_Id(user.getId(), routeId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Saved route was not found"
                        )
                );

        savedRouteRepository.delete(savedRoute);
    }

    private SavedRouteResponse toResponse(SavedRoute savedRoute) {
        return SavedRouteResponse.builder()
                .id(savedRoute.getId())
                .route(routeMapper.toResponse(savedRoute.getRoute()))
                .createdAt(savedRoute.getCreatedAt())
                .build();
    }

    private User getCurrentUser(String email) {
        return userRepository
                .findByEmailIgnoreCaseAndEnabledTrue(email)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Authenticated user was not found"
                        )
                );
    }

    private Route getActiveRoute(Long routeId) {
        return routeRepository
                .findByIdAndActiveTrue(routeId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Route was not found"
                        )
                );
    }
}