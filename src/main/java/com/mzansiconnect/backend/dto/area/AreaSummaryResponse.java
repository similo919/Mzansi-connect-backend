package com.mzansiconnect.backend.dto.area;

import com.mzansiconnect.backend.enums.AreaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaSummaryResponse {

    private Long id;
    private String name;
    private AreaType areaType;
}
