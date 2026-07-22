package com.mzansiconnect.backend.mapper;

import com.mzansiconnect.backend.dto.area.AreaCreateRequest;
import com.mzansiconnect.backend.dto.area.AreaResponse;
import com.mzansiconnect.backend.dto.area.AreaSummaryResponse;
import com.mzansiconnect.backend.dto.area.AreaUpdateRequest;
import com.mzansiconnect.backend.entity.Area;
import org.springframework.stereotype.Component;

@Component
public class AreaMapper {

    public Area toEntity(
            AreaCreateRequest request,
            Area parentArea
    ) {
        return Area.builder()
                .name(normalizeRequiredText(request.getName()))
                .areaType(request.getAreaType())
                .parentArea(parentArea)
                .province(
                        normalizeRequiredText(
                                request.getProvince()
                        )
                )
                .municipality(
                        normalizeRequiredText(
                                request.getMunicipality()
                        )
                )
                .description(
                        normalizeOptionalText(
                                request.getDescription()
                        )
                )
                .active(true)
                .build();
    }

    public void updateEntity(
            Area area,
            AreaUpdateRequest request,
            Area parentArea
    ) {
        area.setName(
                normalizeRequiredText(request.getName())
        );

        area.setAreaType(request.getAreaType());
        area.setParentArea(parentArea);

        area.setProvince(
                normalizeRequiredText(
                        request.getProvince()
                )
        );

        area.setMunicipality(
                normalizeRequiredText(
                        request.getMunicipality()
                )
        );

        area.setDescription(
                normalizeOptionalText(
                        request.getDescription()
                )
        );
    }

    public AreaResponse toResponse(Area area) {
        return AreaResponse.builder()
                .id(area.getId())
                .name(area.getName())
                .areaType(area.getAreaType())
                .parentArea(
                        toSummary(area.getParentArea())
                )
                .province(area.getProvince())
                .municipality(area.getMunicipality())
                .description(area.getDescription())
                .active(area.getActive())
                .build();
    }

    private AreaSummaryResponse toSummary(Area area) {
        if (area == null) {
            return null;
        }

        return AreaSummaryResponse.builder()
                .id(area.getId())
                .name(area.getName())
                .areaType(area.getAreaType())
                .build();
    }

    private String normalizeRequiredText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
