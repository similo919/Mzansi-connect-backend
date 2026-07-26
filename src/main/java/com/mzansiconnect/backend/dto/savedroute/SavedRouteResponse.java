package com.mzansiconnect.backend.dto.savedroute;

import com.mzansiconnect.backend.dto.route.RouteResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedRouteResponse {

    private Long id;
    private RouteResponse route;
    private LocalDateTime createdAt;
}