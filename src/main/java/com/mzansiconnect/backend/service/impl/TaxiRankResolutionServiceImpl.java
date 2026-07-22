package com.mzansiconnect.backend.service.impl;

import com.mzansiconnect.backend.dto.area.AreaSummaryResponse;
import com.mzansiconnect.backend.dto.rank.ResolvedTaxiRankResponse;
import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.entity.AreaRankAssignment;
import com.mzansiconnect.backend.enums.AssignmentType;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.exception.RankAssignmentNotFoundException;
import com.mzansiconnect.backend.exception.ResourceNotFoundException;
import com.mzansiconnect.backend.mapper.TaxiRankMapper;
import com.mzansiconnect.backend.repository.AreaRankAssignmentRepository;
import com.mzansiconnect.backend.repository.AreaRepository;
import com.mzansiconnect.backend.service.TaxiRankResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaxiRankResolutionServiceImpl
        implements TaxiRankResolutionService {

    private static final int MAX_HIERARCHY_DEPTH = 20;

    private final AreaRepository areaRepository;

    private final AreaRankAssignmentRepository
            assignmentRepository;

    private final TaxiRankMapper taxiRankMapper;

    @Override
    public ResolvedTaxiRankResponse resolveTaxiRank(
            Long areaId
    ) {
        Area selectedArea = getActiveArea(areaId);

        Area currentArea = selectedArea;

        Set<Long> visitedAreaIds = new HashSet<>();

        int hierarchyLevelsTraversed = 0;

        while (currentArea != null) {
            validateHierarchyTraversal(
                    currentArea,
                    visitedAreaIds,
                    hierarchyLevelsTraversed
            );

            AreaRankAssignment assignment =
                    findBestAssignment(currentArea);

            if (assignment != null) {
                return buildResponse(
                        selectedArea,
                        currentArea,
                        assignment,
                        hierarchyLevelsTraversed
                );
            }

            Area parentArea = currentArea.getParentArea();

            if (parentArea == null) {
                break;
            }

            currentArea = getActiveArea(
                    parentArea.getId()
            );

            hierarchyLevelsTraversed++;
        }

        throw new RankAssignmentNotFoundException(
                "No active taxi-rank assignment was found for area "
                        + selectedArea.getName()
                        + " or any of its parent areas"
        );
    }

    private AreaRankAssignment findBestAssignment(
            Area area
    ) {
        List<AreaRankAssignment> assignments =
                assignmentRepository
                        .findByArea_IdAndActiveTrueOrderByPriorityAscIdAsc(
                                area.getId()
                        );

        return assignments.stream()
                .filter(this::hasActiveTaxiRank)
                .min(
                        Comparator
                                .comparing(
                                        AreaRankAssignment::getPriority,
                                        Comparator.nullsLast(
                                                Integer::compareTo
                                        )
                                )
                                .thenComparingInt(
                                        assignment ->
                                                assignmentTypeOrder(
                                                        assignment
                                                                .getAssignmentType()
                                                )
                                )
                                .thenComparing(
                                        AreaRankAssignment::getId,
                                        Comparator.nullsLast(
                                                Long::compareTo
                                        )
                                )
                )
                .orElse(null);
    }

    private boolean hasActiveTaxiRank(
            AreaRankAssignment assignment
    ) {
        return assignment.getTaxiRank() != null
                && Boolean.TRUE.equals(
                        assignment
                                .getTaxiRank()
                                .getActive()
                );
    }

    private int assignmentTypeOrder(
            AssignmentType assignmentType
    ) {
        if (assignmentType == null) {
            return Integer.MAX_VALUE;
        }

        return switch (assignmentType) {
            case PRIMARY -> 1;
            case SECONDARY -> 2;
            case INHERITED -> 3;
            case NEAREST_MALL_FALLBACK -> 4;
            case NEAREST_MAJOR_FALLBACK -> 5;
        };
    }

    private void validateHierarchyTraversal(
            Area currentArea,
            Set<Long> visitedAreaIds,
            int hierarchyLevelsTraversed
    ) {
        if (!visitedAreaIds.add(currentArea.getId())) {
            throw new BusinessValidationException(
                    "A loop was detected in the area hierarchy"
            );
        }

        if (hierarchyLevelsTraversed
                > MAX_HIERARCHY_DEPTH) {

            throw new BusinessValidationException(
                    "Area hierarchy exceeds the maximum supported depth"
            );
        }
    }

    private ResolvedTaxiRankResponse buildResponse(
            Area selectedArea,
            Area matchedArea,
            AreaRankAssignment assignment,
            int hierarchyLevelsTraversed
    ) {
        return ResolvedTaxiRankResponse.builder()
                .selectedArea(
                        toAreaSummary(selectedArea)
                )
                .matchedArea(
                        toAreaSummary(matchedArea)
                )
                .taxiRank(
                        taxiRankMapper.toResponse(
                                assignment.getTaxiRank()
                        )
                )
                .assignmentType(
                        assignment.getAssignmentType()
                )
                .priority(assignment.getPriority())
                .hierarchyLevelsTraversed(
                        hierarchyLevelsTraversed
                )
                .inheritedFromParent(
                        hierarchyLevelsTraversed > 0
                )
                .walkingNotes(
                        assignment.getWalkingNotes()
                )
                .assignmentReason(
                        assignment.getAssignmentReason()
                )
                .assignmentLocallyVerified(
                        assignment.getLocallyVerified()
                )
                .assignmentLastVerifiedAt(
                        assignment.getLastVerifiedAt()
                )
                .build();
    }

    private AreaSummaryResponse toAreaSummary(
            Area area
    ) {
        return AreaSummaryResponse.builder()
                .id(area.getId())
                .name(area.getName())
                .areaType(area.getAreaType())
                .build();
    }

    private Area getActiveArea(Long id) {
        return areaRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Active area not found with ID: "
                                        + id
                        )
                );
    }
}
