package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.savedroute.SavedRouteResponse;

import java.util.List;

public interface SavedRouteService {

    SavedRouteResponse saveRoute(String email, Long routeId);

    List<SavedRouteResponse> getSavedRoutes(String email);

    void deleteSavedRoute(String email, Long routeId);
}