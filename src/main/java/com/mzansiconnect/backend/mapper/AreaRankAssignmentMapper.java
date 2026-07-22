package com.mzansiconnect.backend.mapper;

import com.mzansiconnect.backend.dto.area.AreaSummaryResponse;
import com.mzansiconnect.backend.dto.assignment.AreaRankAssignmentCreateRequest;
import com.mzansiconnect.backend.dto.assignment.AreaRankAssignmentResponse;
import com.mzansiconnect.backend.dto.assignment.AreaRankAssignmentUpdateRequest;
import com.mzansiconnect.backend.dto.rank.TaxiRankSummaryResponse;
import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.entity.AreaRankAssignment;
import com.mzansiconnect.backend.entity.TaxiRank;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AreaRankAssignmentMapper {

    public AreaRankAssignment toEntity(
            AreaRankAssignmentCreateRequest request,
            Area area,
            TaxiRank taxiRank
    ) {
        boolean locallyVerified =
                Boolean.TRUE.equals(
                        request.getLocallyVerified()
                );

        return AreaRankAssignment.builder()
                .area(area)
                .taxiRank(taxiRank)
                .assignmentType(
                        request.getAssignmentType()
                )
                .priority(request.getPriority())
                .walkingNotes(
                        normalizeOptionalText(
                                request.getWalkingNotes()
                        )
                )
                .assignmentReason(
                        normalizeOptionalText(
                                request.getAssignmentReason()
                        )
                )
                .active(true)
                .locallyVerified(locallyVerified)
                .lastVerifiedAt(
                        locallyVerified
                                ? LocalDateTime.now()
                                : null
                )
                .build();
    }

    public void updateEntity(
            AreaRankAssignment assignment,
            AreaRankAssignmentUpdateRequest request,
            Area area,
            TaxiRank taxiRank
    ) {
        assignment.setArea(area);
        assignment.setTaxiRank(taxiRank);

        assignment.setAssignmentType(
                request.getAssignmentType()
        );

        assignment.setPriority(
                request.getPriority()
        );

        assignment.setWalkingNotes(
                normalizeOptionalText(
                        request.getWalkingNotes()
                )
        );

        assignment.setAssignmentReason(
                normalizeOptionalText(
                        request.getAssignmentReason()
                )
        );

        if (request.getLocallyVerified() != null) {
            updateVerificationState(
                    assignment,
                    request.getLocallyVerified()
            );
        }
    }

    public AreaRankAssignmentResponse toResponse(
            AreaRankAssignment assignment
    ) {
        return AreaRankAssignmentResponse.builder()
                .id(assignment.getId())
                .area(
                        toAreaSummary(
                                assignment.getArea()
                        )
                )
                .taxiRank(
                        toTaxiRankSummary(
                                assignment.getTaxiRank()
                        )
                )
                .assignmentType(
                        assignment.getAssignmentType()
                )
                .priority(assignment.getPriority())
                .walkingNotes(
                        assignment.getWalkingNotes()
                )
                .assignmentReason(
                        assignment.getAssignmentReason()
                )
                .active(assignment.getActive())
                .locallyVerified(
                        assignment.getLocallyVerified()
                )
                .lastVerifiedAt(
                        assignment.getLastVerifiedAt()
                )
                .build();
    }

    private AreaSummaryResponse toAreaSummary(
            Area area
    ) {
        if (area == null) {
            return null;
        }

        return AreaSummaryResponse.builder()
                .id(area.getId())
                .name(area.getName())
                .areaType(area.getAreaType())
                .build();
    }

    private TaxiRankSummaryResponse toTaxiRankSummary(
            TaxiRank taxiRank
    ) {
        if (taxiRank == null) {
            return null;
        }

        return TaxiRankSummaryResponse.builder()
                .id(taxiRank.getId())
                .name(taxiRank.getName())
                .rankType(taxiRank.getRankType())
                .build();
    }

    private void updateVerificationState(
            AreaRankAssignment assignment,
            Boolean locallyVerified
    ) {
        if (Boolean.TRUE.equals(locallyVerified)) {
            assignment.setLocallyVerified(true);

            if (assignment.getLastVerifiedAt() == null) {
                assignment.setLastVerifiedAt(
                        LocalDateTime.now()
                );
            }
        } else {
            assignment.setLocallyVerified(false);
            assignment.setLastVerifiedAt(null);
        }
    }

    private String normalizeOptionalText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
