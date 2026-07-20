package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.dto.area.AreaCreateRequest;
import com.mzansiconnect.backend.dto.area.AreaResponse;
import com.mzansiconnect.backend.dto.area.AreaUpdateRequest;
import com.mzansiconnect.backend.enums.AreaType;
import com.mzansiconnect.backend.response.ApiResponse;
import com.mzansiconnect.backend.service.AreaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/areas")
@RequiredArgsConstructor
@Validated
public class AreaController {

    private final AreaService areaService;

    @PostMapping
    public ResponseEntity<ApiResponse<AreaResponse>>
    createArea(
            @Valid
            @RequestBody
            AreaCreateRequest request
    ) {
        AreaResponse response =
                areaService.createArea(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Area created successfully",
                                response
                        )
                );
    }

    @GetMapping
    public ResponseEntity<
            ApiResponse<Page<AreaResponse>>
            > getAreas(
            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            AreaType areaType,

            @RequestParam(required = false)
            Long parentAreaId,

            @RequestParam(defaultValue = "0")
            @Min(
                    value = 0,
                    message = "Page number cannot be negative"
            )
            int page,

            @RequestParam(defaultValue = "20")
            @Min(
                    value = 1,
                    message = "Page size must be at least 1"
            )
            @Max(
                    value = 100,
                    message = "Page size cannot exceed 100"
            )
            int size,

            @RequestParam(defaultValue = "name")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String sortDirection
    ) {
        Page<AreaResponse> response =
                areaService.getAreas(
                        search,
                        areaType,
                        parentAreaId,
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Areas retrieved successfully",
                        response
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AreaResponse>>
    getAreaById(
            @PathVariable
            @Positive(message = "Area ID must be positive")
            Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Area retrieved successfully",
                        areaService.getAreaById(id)
                )
        );
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<
            ApiResponse<List<AreaResponse>>
            > getChildAreas(
            @PathVariable
            @Positive(message = "Area ID must be positive")
            Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Child areas retrieved successfully",
                        areaService.getChildAreas(id)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AreaResponse>>
    updateArea(
            @PathVariable
            @Positive(message = "Area ID must be positive")
            Long id,

            @Valid
            @RequestBody
            AreaUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Area updated successfully",
                        areaService.updateArea(
                                id,
                                request
                        )
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>>
    deactivateArea(
            @PathVariable
            @Positive(message = "Area ID must be positive")
            Long id
    ) {
        areaService.deactivateArea(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Area deactivated successfully"
                )
        );
    }
}
