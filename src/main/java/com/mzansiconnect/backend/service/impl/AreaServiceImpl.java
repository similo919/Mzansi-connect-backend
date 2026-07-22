package com.mzansiconnect.backend.service.impl;

import com.mzansiconnect.backend.dto.area.AreaCreateRequest;
import com.mzansiconnect.backend.dto.area.AreaResponse;
import com.mzansiconnect.backend.dto.area.AreaUpdateRequest;
import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.enums.AreaType;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.exception.DuplicateResourceException;
import com.mzansiconnect.backend.exception.ResourceNotFoundException;
import com.mzansiconnect.backend.mapper.AreaMapper;
import com.mzansiconnect.backend.repository.AreaRankAssignmentRepository;
import com.mzansiconnect.backend.repository.AreaRepository;
import com.mzansiconnect.backend.repository.TaxiRankRepository;
import com.mzansiconnect.backend.service.AreaService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AreaServiceImpl implements AreaService {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(
                    "id",
                    "name",
                    "areaType",
                    "province",
                    "municipality"
            );

    private final AreaRepository areaRepository;
    private final TaxiRankRepository taxiRankRepository;
    private final AreaRankAssignmentRepository
            areaRankAssignmentRepository;

    private final AreaMapper areaMapper;

    @Override
    public AreaResponse createArea(
            AreaCreateRequest request
    ) {
        Area parentArea =
                resolveParentArea(request.getParentAreaId());

        validateHierarchy(
                request.getAreaType(),
                parentArea,
                null
        );

        validateDuplicateName(
                request.getName(),
                request.getParentAreaId(),
                null
        );

        Area area =
                areaMapper.toEntity(
                        request,
                        parentArea
                );

        return areaMapper.toResponse(
                areaRepository.save(area)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AreaResponse getAreaById(Long id) {
        return areaMapper.toResponse(
                getActiveArea(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AreaResponse> getAreas(
            String search,
            AreaType areaType,
            Long parentAreaId,
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

        Specification<Area> specification =
                createSpecification(
                        search,
                        areaType,
                        parentAreaId
                );

        return areaRepository
                .findAll(specification, pageable)
                .map(areaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaResponse> getChildAreas(
            Long parentAreaId
    ) {
        getActiveArea(parentAreaId);

        return areaRepository
                .findByParentArea_IdAndActiveTrueOrderByNameAsc(
                        parentAreaId
                )
                .stream()
                .map(areaMapper::toResponse)
                .toList();
    }

    @Override
    public AreaResponse updateArea(
            Long id,
            AreaUpdateRequest request
    ) {
        Area area = getActiveArea(id);

        Area parentArea =
                resolveParentArea(request.getParentAreaId());

        validateHierarchy(
                request.getAreaType(),
                parentArea,
                id
        );

        validateDuplicateName(
                request.getName(),
                request.getParentAreaId(),
                id
        );

        areaMapper.updateEntity(
                area,
                request,
                parentArea
        );

        return areaMapper.toResponse(
                areaRepository.save(area)
        );
    }

    @Override
    public void deactivateArea(Long id) {
        Area area = getActiveArea(id);

        boolean hasActiveChildren =
                !areaRepository
                        .findByParentArea_IdAndActiveTrueOrderByNameAsc(
                                id
                        )
                        .isEmpty();

        if (hasActiveChildren) {
            throw new BusinessValidationException(
                    "Area cannot be deactivated while it has active child areas"
            );
        }

        boolean hasActiveTaxiRanks =
                !taxiRankRepository
                        .findByLocatedInArea_IdAndActiveTrueOrderByNameAsc(
                                id
                        )
                        .isEmpty();

        if (hasActiveTaxiRanks) {
            throw new BusinessValidationException(
                    "Area cannot be deactivated while it has active taxi ranks"
            );
        }

        boolean hasActiveRankAssignments =
                !areaRankAssignmentRepository
                        .findByArea_IdAndActiveTrueOrderByPriorityAscIdAsc(
                                id
                        )
                        .isEmpty();

        if (hasActiveRankAssignments) {
            throw new BusinessValidationException(
                    "Area cannot be deactivated while it has active taxi-rank assignments"
            );
        }

        area.setActive(false);
        areaRepository.save(area);
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

    private Area resolveParentArea(Long parentAreaId) {
        if (parentAreaId == null) {
            return null;
        }

        return getActiveArea(parentAreaId);
    }

    private void validateDuplicateName(
            String areaName,
            Long parentAreaId,
            Long excludedAreaId
    ) {
        String normalizedName = areaName.trim();

        boolean duplicate;

        if (parentAreaId == null) {
            duplicate = excludedAreaId == null
                    ? areaRepository
                    .existsByNameIgnoreCaseAndParentAreaIsNull(
                            normalizedName
                    )
                    : areaRepository
                    .existsByNameIgnoreCaseAndParentAreaIsNullAndIdNot(
                            normalizedName,
                            excludedAreaId
                    );
        } else {
            duplicate = excludedAreaId == null
                    ? areaRepository
                    .existsByNameIgnoreCaseAndParentArea_Id(
                            normalizedName,
                            parentAreaId
                    )
                    : areaRepository
                    .existsByNameIgnoreCaseAndParentArea_IdAndIdNot(
                            normalizedName,
                            parentAreaId,
                            excludedAreaId
                    );
        }

        if (duplicate) {
            throw new DuplicateResourceException(
                    "An area with this name already exists under the selected parent"
            );
        }
    }

    private void validateHierarchy(
            AreaType areaType,
            Area parentArea,
            Long currentAreaId
    ) {
        if (areaType == AreaType.REGION
                && parentArea != null) {
            throw new BusinessValidationException(
                    "A REGION area cannot have a parent area"
            );
        }

        if ((areaType == AreaType.ZONE
                || areaType == AreaType.EXTENSION)
                && parentArea == null) {

            throw new BusinessValidationException(
                    areaType
                            + " areas must belong to a parent township"
            );
        }

        if ((areaType == AreaType.ZONE
                || areaType == AreaType.EXTENSION)
                && parentArea != null
                && parentArea.getAreaType()
                != AreaType.SUBURB) {

            throw new BusinessValidationException(
                    areaType
                            + " areas must have a SUBURB as their direct parent"
            );
        }

        if (currentAreaId != null
                && parentArea != null) {
            validateNoHierarchyCycle(
                    currentAreaId,
                    parentArea
            );
        }
    }

    private void validateNoHierarchyCycle(
            Long currentAreaId,
            Area proposedParent
    ) {
        Set<Long> visitedAreaIds =
                new HashSet<>();

        Area current = proposedParent;

        while (current != null) {
            if (currentAreaId.equals(current.getId())) {
                throw new BusinessValidationException(
                        "An area cannot be its own parent or descendant"
                );
            }

            if (!visitedAreaIds.add(current.getId())) {
                throw new BusinessValidationException(
                        "A loop was detected in the area hierarchy"
                );
            }

            current = current.getParentArea();
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
                        ? "name"
                        : sortBy.trim();

        if (!ALLOWED_SORT_FIELDS.contains(safeSortBy)) {
            throw new BusinessValidationException(
                    "Unsupported area sort field: "
                            + safeSortBy
            );
        }

        Sort.Direction direction;

        try {
            direction = Sort.Direction.fromString(
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

    private Specification<Area> createSpecification(
            String search,
            AreaType areaType,
            Long parentAreaId
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates =
                    new ArrayList<>();

            predicates.add(
                    criteriaBuilder.isTrue(
                            root.get("active")
                    )
            );

            if (search != null
                    && !search.isBlank()) {
                String pattern =
                        "%"
                                + search.trim()
                                .toLowerCase(Locale.ROOT)
                                + "%";

                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("name")
                                ),
                                pattern
                        )
                );
            }

            if (areaType != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("areaType"),
                                areaType
                        )
                );
            }

            if (parentAreaId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("parentArea")
                                        .get("id"),
                                parentAreaId
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
