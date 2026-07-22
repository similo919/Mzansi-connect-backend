package com.mzansiconnect.backend.service.impl;

import com.mzansiconnect.backend.dto.assignment.AreaRankAssignmentCreateRequest;
import com.mzansiconnect.backend.dto.assignment.AreaRankAssignmentResponse;
import com.mzansiconnect.backend.dto.assignment.AreaRankAssignmentUpdateRequest;
import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.entity.AreaRankAssignment;
import com.mzansiconnect.backend.entity.TaxiRank;
import com.mzansiconnect.backend.enums.AreaType;
import com.mzansiconnect.backend.enums.AssignmentType;
import com.mzansiconnect.backend.enums.RankType;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.exception.DuplicateResourceException;
import com.mzansiconnect.backend.exception.ResourceNotFoundException;
import com.mzansiconnect.backend.mapper.AreaRankAssignmentMapper;
import com.mzansiconnect.backend.repository.AreaRankAssignmentRepository;
import com.mzansiconnect.backend.repository.AreaRepository;
import com.mzansiconnect.backend.repository.TaxiRankRepository;
import com.mzansiconnect.backend.service.AreaRankAssignmentService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AreaRankAssignmentServiceImpl
        implements AreaRankAssignmentService {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String>
            ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "priority",
            "assignmentType",
            "locallyVerified",
            "lastVerifiedAt"
    );

    private final AreaRankAssignmentRepository
            assignmentRepository;

    private final AreaRepository areaRepository;

    private final TaxiRankRepository taxiRankRepository;

    private final AreaRankAssignmentMapper
            assignmentMapper;

    @Override
    public AreaRankAssignmentResponse createAssignment(
            AreaRankAssignmentCreateRequest request
    ) {
        Area area =
                getActiveArea(request.getAreaId());

        TaxiRank taxiRank =
                getActiveTaxiRank(
                        request.getTaxiRankId()
                );

        validateDuplicateAssignment(
                request.getAreaId(),
                request.getTaxiRankId(),
                null
        );

        validateAssignmentRules(
                area,
                taxiRank,
                request.getAssignmentType(),
                request.getAssignmentReason()
        );

        AreaRankAssignment assignment =
                assignmentMapper.toEntity(
                        request,
                        area,
                        taxiRank
                );

        return assignmentMapper.toResponse(
                assignmentRepository.save(assignment)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AreaRankAssignmentResponse
    getAssignmentById(Long id) {
        return assignmentMapper.toResponse(
                getActiveAssignment(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AreaRankAssignmentResponse>
    getAssignments(
            Long areaId,
            Long taxiRankId,
            AssignmentType assignmentType,
            Boolean locallyVerified,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Pageable pageable =
                createPageable(
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        Specification<AreaRankAssignment>
                specification =
                createSpecification(
                        areaId,
                        taxiRankId,
                        assignmentType,
                        locallyVerified
                );

        return assignmentRepository
                .findAll(specification, pageable)
                .map(assignmentMapper::toResponse);
    }

    @Override
    public AreaRankAssignmentResponse
    updateAssignment(
            Long id,
            AreaRankAssignmentUpdateRequest request
    ) {
        AreaRankAssignment assignment =
                getActiveAssignment(id);

        Area area =
                getActiveArea(request.getAreaId());

        TaxiRank taxiRank =
                getActiveTaxiRank(
                        request.getTaxiRankId()
                );

        validateDuplicateAssignment(
                request.getAreaId(),
                request.getTaxiRankId(),
                id
        );

        validateAssignmentRules(
                area,
                taxiRank,
                request.getAssignmentType(),
                request.getAssignmentReason()
        );

        assignmentMapper.updateEntity(
                assignment,
                request,
                area,
                taxiRank
        );

        return assignmentMapper.toResponse(
                assignmentRepository.save(assignment)
        );
    }

    @Override
    public void deactivateAssignment(Long id) {
        AreaRankAssignment assignment =
                getActiveAssignment(id);

        assignment.setActive(false);

        assignmentRepository.save(assignment);
    }

    private AreaRankAssignment getActiveAssignment(
            Long id
    ) {
        return assignmentRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Active area-rank assignment not found with ID: "
                                                + id
                                )
                );
    }

    private Area getActiveArea(Long id) {
        return areaRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Active area not found with ID: "
                                                + id
                                )
                );
    }

    private TaxiRank getActiveTaxiRank(Long id) {
        return taxiRankRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Active taxi rank not found with ID: "
                                                + id
                                )
                );
    }

    private void validateDuplicateAssignment(
            Long areaId,
            Long taxiRankId,
            Long excludedAssignmentId
    ) {
        boolean duplicate;

        if (excludedAssignmentId == null) {
            duplicate = assignmentRepository
                    .existsByArea_IdAndTaxiRank_IdAndActiveTrue(
                            areaId,
                            taxiRankId
                    );
        } else {
            duplicate = assignmentRepository
                    .existsByArea_IdAndTaxiRank_IdAndIdNotAndActiveTrue(
                            areaId,
                            taxiRankId,
                            excludedAssignmentId
                    );
        }

        if (duplicate) {
            throw new DuplicateResourceException(
                    "This area already has an active assignment to the selected taxi rank"
            );
        }
    }

    private void validateAssignmentRules(
            Area area,
            TaxiRank taxiRank,
            AssignmentType assignmentType,
            String assignmentReason
    ) {
        if (assignmentType
                == AssignmentType.NEAREST_MALL_FALLBACK
                && taxiRank.getRankType()
                != RankType.MALL_RANK) {

            throw new BusinessValidationException(
                    "NEAREST_MALL_FALLBACK must reference a MALL_RANK"
            );
        }

        if (assignmentType
                == AssignmentType.NEAREST_MAJOR_FALLBACK
                && taxiRank.getRankType()
                != RankType.MAJOR_RANK
                && taxiRank.getRankType()
                != RankType.HOSPITAL_RANK) {

            throw new BusinessValidationException(
                    "NEAREST_MAJOR_FALLBACK must reference a MAJOR_RANK or HOSPITAL_RANK"
            );
        }

        if (assignmentType == AssignmentType.INHERITED) {
            validateInheritedAssignment(
                    area,
                    taxiRank
            );
        }

        boolean reasonRequired =
                assignmentType
                        == AssignmentType.INHERITED
                        || assignmentType
                        == AssignmentType.NEAREST_MALL_FALLBACK
                        || assignmentType
                        == AssignmentType.NEAREST_MAJOR_FALLBACK;

        if (reasonRequired
                && (assignmentReason == null
                || assignmentReason.isBlank())) {

            throw new BusinessValidationException(
                    "Assignment reason is required for inherited and fallback assignments"
            );
        }
    }

    private void validateInheritedAssignment(
            Area area,
            TaxiRank taxiRank
    ) {
        if (area.getAreaType() != AreaType.ZONE
                && area.getAreaType()
                != AreaType.EXTENSION) {

            throw new BusinessValidationException(
                    "INHERITED assignments may only be created for ZONE or EXTENSION areas"
            );
        }

        if (area.getParentArea() == null) {
            throw new BusinessValidationException(
                    "An inherited assignment requires a parent township"
            );
        }

        boolean parentUsesSameRank =
                assignmentRepository
                        .existsByArea_IdAndTaxiRank_IdAndActiveTrue(
                                area.getParentArea().getId(),
                                taxiRank.getId()
                        );

        if (!parentUsesSameRank) {
            throw new BusinessValidationException(
                    "The selected taxi rank is not actively assigned to the parent township"
            );
        }
    }

    private Pageable createPageable(
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        if (page < 0) {
            throw new BusinessValidationException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessValidationException(
                    "Page size must be between 1 and "
                            + MAX_PAGE_SIZE
            );
        }

        String safeSortBy =
                sortBy == null || sortBy.isBlank()
                        ? "priority"
                        : sortBy.trim();

        if (!ALLOWED_SORT_FIELDS.contains(
                safeSortBy
        )) {
            throw new BusinessValidationException(
                    "Unsupported assignment sort field: "
                            + safeSortBy
            );
        }

        Sort.Direction direction;

        try {
            direction =
                    Sort.Direction.fromString(
                            sortDirection == null
                                    ? "asc"
                                    : sortDirection
                    );
        } catch (IllegalArgumentException exception) {
            throw new BusinessValidationException(
                    "Sort direction must be asc or desc"
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(direction, safeSortBy)
        );
    }

    private Specification<AreaRankAssignment>
    createSpecification(
            Long areaId,
            Long taxiRankId,
            AssignmentType assignmentType,
            Boolean locallyVerified
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates =
                    new ArrayList<>();

            predicates.add(
                    criteriaBuilder.isTrue(
                            root.get("active")
                    )
            );

            if (areaId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("area")
                                        .get("id"),
                                areaId
                        )
                );
            }

            if (taxiRankId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("taxiRank")
                                        .get("id"),
                                taxiRankId
                        )
                );
            }

            if (assignmentType != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("assignmentType"),
                                assignmentType
                        )
                );
            }

            if (locallyVerified != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("locallyVerified"),
                                locallyVerified
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(
                            new Predicate[0]
                    )
            );
        };
    }
}
