package com.mzansiconnect.backend.service.impl;

import com.mzansiconnect.backend.dto.rank.TaxiRankCreateRequest;
import com.mzansiconnect.backend.dto.rank.TaxiRankResponse;
import com.mzansiconnect.backend.dto.rank.TaxiRankUpdateRequest;
import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.entity.TaxiRank;
import com.mzansiconnect.backend.enums.AreaType;
import com.mzansiconnect.backend.enums.RankType;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.exception.DuplicateResourceException;
import com.mzansiconnect.backend.exception.ResourceNotFoundException;
import com.mzansiconnect.backend.mapper.TaxiRankMapper;
import com.mzansiconnect.backend.repository.AreaRankAssignmentRepository;
import com.mzansiconnect.backend.repository.AreaRepository;
import com.mzansiconnect.backend.repository.TaxiRankRepository;
import com.mzansiconnect.backend.service.TaxiRankService;
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
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class TaxiRankServiceImpl
        implements TaxiRankService {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String>
            ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "name",
            "rankType",
            "formalRank",
            "locallyVerified",
            "lastVerifiedAt"
    );

    private static final Set<AreaType>
            ALLOWED_RANK_AREA_TYPES = Set.of(
            AreaType.SUBURB,
            AreaType.TOWN,
            AreaType.CITY
    );

    private final TaxiRankRepository taxiRankRepository;
    private final AreaRepository areaRepository;

    private final AreaRankAssignmentRepository
            areaRankAssignmentRepository;

    private final TaxiRankMapper taxiRankMapper;

    @Override
    public TaxiRankResponse createTaxiRank(
            TaxiRankCreateRequest request
    ) {
        Area locatedInArea =
                getActiveArea(
                        request.getLocatedInAreaId()
                );

        validateRankLocation(locatedInArea);

        validateDuplicateRank(
                request.getName(),
                request.getLocatedInAreaId(),
                null
        );

        TaxiRank taxiRank =
                taxiRankMapper.toEntity(
                        request,
                        locatedInArea
                );

        return taxiRankMapper.toResponse(
                taxiRankRepository.save(taxiRank)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TaxiRankResponse getTaxiRankById(
            Long id
    ) {
        return taxiRankMapper.toResponse(
                getActiveTaxiRank(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaxiRankResponse> getTaxiRanks(
            String search,
            RankType rankType,
            Long locatedInAreaId,
            Boolean formalRank,
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

        Specification<TaxiRank> specification =
                createSpecification(
                        search,
                        rankType,
                        locatedInAreaId,
                        formalRank,
                        locallyVerified
                );

        return taxiRankRepository
                .findAll(specification, pageable)
                .map(taxiRankMapper::toResponse);
    }

    @Override
    public TaxiRankResponse updateTaxiRank(
            Long id,
            TaxiRankUpdateRequest request
    ) {
        TaxiRank taxiRank =
                getActiveTaxiRank(id);

        Area locatedInArea =
                getActiveArea(
                        request.getLocatedInAreaId()
                );

        validateRankLocation(locatedInArea);

        validateDuplicateRank(
                request.getName(),
                request.getLocatedInAreaId(),
                id
        );

        taxiRankMapper.updateEntity(
                taxiRank,
                request,
                locatedInArea
        );

        return taxiRankMapper.toResponse(
                taxiRankRepository.save(taxiRank)
        );
    }

    @Override
    public void deactivateTaxiRank(Long id) {
        TaxiRank taxiRank =
                getActiveTaxiRank(id);

        boolean hasActiveAssignments =
                areaRankAssignmentRepository
                        .existsByTaxiRank_IdAndActiveTrue(id);

        if (hasActiveAssignments) {
            throw new BusinessValidationException(
                    "Taxi rank cannot be deactivated while it has active area assignments"
            );
        }

        taxiRank.setActive(false);
        taxiRankRepository.save(taxiRank);
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

    private void validateRankLocation(
            Area locatedInArea
    ) {
        if (!ALLOWED_RANK_AREA_TYPES.contains(
                locatedInArea.getAreaType()
        )) {
            throw new BusinessValidationException(
                    "Taxi ranks must be attached to a SUBURB, TOWN or CITY, not a zone, extension or region"
            );
        }
    }

    private void validateDuplicateRank(
            String name,
            Long locatedInAreaId,
            Long excludedTaxiRankId
    ) {
        String normalizedName = name.trim();

        boolean duplicate;

        if (excludedTaxiRankId == null) {
            duplicate = taxiRankRepository
                    .existsByNameIgnoreCaseAndLocatedInArea_Id(
                            normalizedName,
                            locatedInAreaId
                    );
        } else {
            duplicate = taxiRankRepository
                    .existsByNameIgnoreCaseAndLocatedInArea_IdAndIdNot(
                            normalizedName,
                            locatedInAreaId,
                            excludedTaxiRankId
                    );
        }

        if (duplicate) {
            throw new DuplicateResourceException(
                    "A taxi rank with this name already exists in the selected area"
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
                        ? "name"
                        : sortBy.trim();

        if (!ALLOWED_SORT_FIELDS.contains(
                safeSortBy
        )) {
            throw new BusinessValidationException(
                    "Unsupported taxi-rank sort field: "
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

    private Specification<TaxiRank>
    createSpecification(
            String search,
            RankType rankType,
            Long locatedInAreaId,
            Boolean formalRank,
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

            if (search != null
                    && !search.isBlank()) {

                String pattern =
                        "%"
                                + search.trim()
                                .toLowerCase(Locale.ROOT)
                                + "%";

                Predicate nameMatch =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("name")
                                ),
                                pattern
                        );

                Predicate addressMatch =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get(
                                                "addressDescription"
                                        )
                                ),
                                pattern
                        );

                predicates.add(
                        criteriaBuilder.or(
                                nameMatch,
                                addressMatch
                        )
                );
            }

            if (rankType != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("rankType"),
                                rankType
                        )
                );
            }

            if (locatedInAreaId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("locatedInArea")
                                        .get("id"),
                                locatedInAreaId
                        )
                );
            }

            if (formalRank != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("formalRank"),
                                formalRank
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
