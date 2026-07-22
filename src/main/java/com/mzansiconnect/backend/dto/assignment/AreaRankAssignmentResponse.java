package com.mzansiconnect.backend.dto.assignment;

import com.mzansiconnect.backend.dto.area.AreaSummaryResponse;
import com.mzansiconnect.backend.dto.rank.TaxiRankSummaryResponse;
import com.mzansiconnect.backend.enums.AssignmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaRankAssignmentResponse {

    private Long id;

    private AreaSummaryResponse area;

    private TaxiRankSummaryResponse taxiRank;

    private AssignmentType assignmentType;

    private Integer priority;

    private String walkingNotes;

    private String assignmentReason;

    private Boolean active;

    private Boolean locallyVerified;

    private LocalDateTime lastVerifiedAt;
}
