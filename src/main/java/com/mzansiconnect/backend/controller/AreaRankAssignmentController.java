package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.dto.assignment.AreaRankAssignmentCreateRequest;
import com.mzansiconnect.backend.dto.assignment.AreaRankAssignmentResponse;
import com.mzansiconnect.backend.dto.assignment.AreaRankAssignmentUpdateRequest;
import com.mzansiconnect.backend.enums.AssignmentType;
import com.mzansiconnect.backend.response.ApiResponse;
import com.mzansiconnect.backend.service.AreaRankAssignmentService;
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

@RestController
@RequestMapping("/api/area-rank-assignments")
@RequiredArgsConstructor
@Validated
public class AreaRankAssignmentController {

    private final AreaRankAssignmentService
            assignmentService;

    @PostMapping
    public ResponseEntity<
            ApiResponse<AreaRankAssignmentResponse>
            > createAssignment(
            @Valid
            @RequestBody
            AreaRankAssignmentCreateRequest request
    ) {
        AreaRankAssignmentResponse response =
                assignmentService.createAssignment(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Area-rank assignment created successfully",
                                response
                        )
                );
    }

    @GetMapping
    public ResponseEntity<
            ApiResponse<Page<AreaRankAssignmentResponse>>
            > getAssignments(
            @RequestParam(required = false)
            @Positive(message = "Area ID must be positive")
            Long areaId,

            @RequestParam(required = false)
            @Positive(message = "Taxi rank ID must be positive")
            Long taxiRankId,

            @RequestParam(required = false)
            AssignmentType assignmentType,

            @RequestParam(required = false)
            Boolean locallyVerified,

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

            @RequestParam(defaultValue = "priority")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String sortDirection
    ) {
        Page<AreaRankAssignmentResponse> response =
                assignmentService.getAssignments(
                        areaId,
                        taxiRankId,
                        assignmentType,
                        locallyVerified,
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Area-rank assignments retrieved successfully",
                        response
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<
            ApiResponse<AreaRankAssignmentResponse>
            > getAssignmentById(
            @PathVariable
            @Positive(
                    message = "Assignment ID must be positive"
            )
            Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Area-rank assignment retrieved successfully",
                        assignmentService
                                .getAssignmentById(id)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<
            ApiResponse<AreaRankAssignmentResponse>
            > updateAssignment(
            @PathVariable
            @Positive(
                    message = "Assignment ID must be positive"
            )
            Long id,

            @Valid
            @RequestBody
            AreaRankAssignmentUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Area-rank assignment updated successfully",
                        assignmentService.updateAssignment(
                                id,
                                request
                        )
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>>
    deactivateAssignment(
            @PathVariable
            @Positive(
                    message = "Assignment ID must be positive"
            )
            Long id
    ) {
        assignmentService.deactivateAssignment(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Area-rank assignment deactivated successfully"
                )
        );
    }
}
