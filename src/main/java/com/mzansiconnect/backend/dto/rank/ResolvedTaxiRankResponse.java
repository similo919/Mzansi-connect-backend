package com.mzansiconnect.backend.dto.rank;

import com.mzansiconnect.backend.dto.area.AreaSummaryResponse;
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
public class ResolvedTaxiRankResponse {

    private AreaSummaryResponse selectedArea;

    private AreaSummaryResponse matchedArea;

    private TaxiRankResponse taxiRank;

    private AssignmentType assignmentType;

    private Integer priority;

    private Integer hierarchyLevelsTraversed;

    private Boolean inheritedFromParent;

    private String walkingNotes;

    private String assignmentReason;

    private Boolean assignmentLocallyVerified;

    private LocalDateTime assignmentLastVerifiedAt;
}
