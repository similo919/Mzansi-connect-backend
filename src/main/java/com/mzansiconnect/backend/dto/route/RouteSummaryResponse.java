package com.mzansiconnect.backend.dto.route;

import com.mzansiconnect.backend.enums.RouteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteSummaryResponse {

    private Long id;
    private String routeCode;
    private String routeName;
    private RouteType routeType;
}
