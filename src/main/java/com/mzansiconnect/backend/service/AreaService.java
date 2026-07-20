package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.area.AreaCreateRequest;
import com.mzansiconnect.backend.dto.area.AreaResponse;
import com.mzansiconnect.backend.dto.area.AreaUpdateRequest;
import com.mzansiconnect.backend.enums.AreaType;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AreaService {

    AreaResponse createArea(AreaCreateRequest request);

    AreaResponse getAreaById(Long id);

    Page<AreaResponse> getAreas(
            String search,
            AreaType areaType,
            Long parentAreaId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    List<AreaResponse> getChildAreas(Long parentAreaId);

    AreaResponse updateArea(
            Long id,
            AreaUpdateRequest request
    );

    void deactivateArea(Long id);
}
