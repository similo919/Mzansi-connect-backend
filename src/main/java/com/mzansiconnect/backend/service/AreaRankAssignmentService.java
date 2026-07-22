package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.assignment.AreaRankAssignmentCreateRequest;
import com.mzansiconnect.backend.dto.assignment.AreaRankAssignmentResponse;
import com.mzansiconnect.backend.dto.assignment.AreaRankAssignmentUpdateRequest;
import com.mzansiconnect.backend.enums.AssignmentType;
import org.springframework.data.domain.Page;

public interface AreaRankAssignmentService {

    AreaRankAssignmentResponse createAssignment(
            AreaRankAssignmentCreateRequest request
    );

    AreaRankAssignmentResponse getAssignmentById(
            Long id
    );

    Page<AreaRankAssignmentResponse> getAssignments(
            Long areaId,
            Long taxiRankId,
            AssignmentType assignmentType,
            Boolean locallyVerified,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    AreaRankAssignmentResponse updateAssignment(
            Long id,
            AreaRankAssignmentUpdateRequest request
    );

    void deactivateAssignment(Long id);
}
