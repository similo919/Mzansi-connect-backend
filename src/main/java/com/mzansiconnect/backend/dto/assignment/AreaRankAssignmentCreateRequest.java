package com.mzansiconnect.backend.dto.assignment;

import com.mzansiconnect.backend.enums.AssignmentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaRankAssignmentCreateRequest {

    @NotNull(message = "Area ID is required")
    @Positive(message = "Area ID must be positive")
    private Long areaId;

    @NotNull(message = "Taxi rank ID is required")
    @Positive(message = "Taxi rank ID must be positive")
    private Long taxiRankId;

    @NotNull(message = "Assignment type is required")
    private AssignmentType assignmentType;

    @NotNull(message = "Priority is required")
    @Min(
            value = 1,
            message = "Priority must be at least 1"
    )
    @Max(
            value = 100,
            message = "Priority cannot exceed 100"
    )
    private Integer priority;

    @Size(
            max = 500,
            message = "Walking notes cannot exceed 500 characters"
    )
    private String walkingNotes;

    @Size(
            max = 500,
            message = "Assignment reason cannot exceed 500 characters"
    )
    private String assignmentReason;

    private Boolean locallyVerified;
}
