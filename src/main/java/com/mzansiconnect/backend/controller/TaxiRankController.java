package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.dto.rank.TaxiRankCreateRequest;
import com.mzansiconnect.backend.dto.rank.ResolvedTaxiRankResponse;
import com.mzansiconnect.backend.dto.rank.TaxiRankResponse;
import com.mzansiconnect.backend.dto.rank.TaxiRankUpdateRequest;
import com.mzansiconnect.backend.enums.RankType;
import com.mzansiconnect.backend.response.ApiResponse;
import com.mzansiconnect.backend.service.TaxiRankResolutionService;
import com.mzansiconnect.backend.service.TaxiRankService;
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
@RequestMapping("/api/taxi-ranks")
@RequiredArgsConstructor
@Validated
public class TaxiRankController {

    private final TaxiRankService taxiRankService;

    private final TaxiRankResolutionService
            taxiRankResolutionService;

    @PostMapping
    public ResponseEntity<
            ApiResponse<TaxiRankResponse>
            > createTaxiRank(
            @Valid
            @RequestBody
            TaxiRankCreateRequest request
    ) {
        TaxiRankResponse response =
                taxiRankService.createTaxiRank(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Taxi rank created successfully",
                                response
                        )
                );
    }

    @GetMapping
    public ResponseEntity<
            ApiResponse<Page<TaxiRankResponse>>
            > getTaxiRanks(
            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            RankType rankType,

            @RequestParam(required = false)
            Long locatedInAreaId,

            @RequestParam(required = false)
            Boolean formalRank,

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

            @RequestParam(defaultValue = "name")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String sortDirection
    ) {
        Page<TaxiRankResponse> response =
                taxiRankService.getTaxiRanks(
                        search,
                        rankType,
                        locatedInAreaId,
                        formalRank,
                        locallyVerified,
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Taxi ranks retrieved successfully",
                        response
                )
        );
    }

    @GetMapping("/resolve")
    public ResponseEntity<
            ApiResponse<ResolvedTaxiRankResponse>
            > resolveTaxiRank(
            @RequestParam
            @Positive(message = "Area ID must be positive")
            Long areaId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Taxi rank resolved successfully",
                        taxiRankResolutionService
                                .resolveTaxiRank(areaId)
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<
            ApiResponse<TaxiRankResponse>
            > getTaxiRankById(
            @PathVariable
            @Positive(
                    message = "Taxi rank ID must be positive"
            )
            Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Taxi rank retrieved successfully",
                        taxiRankService.getTaxiRankById(id)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<
            ApiResponse<TaxiRankResponse>
            > updateTaxiRank(
            @PathVariable
            @Positive(
                    message = "Taxi rank ID must be positive"
            )
            Long id,

            @Valid
            @RequestBody
            TaxiRankUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Taxi rank updated successfully",
                        taxiRankService.updateTaxiRank(
                                id,
                                request
                        )
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>>
    deactivateTaxiRank(
            @PathVariable
            @Positive(
                    message = "Taxi rank ID must be positive"
            )
            Long id
    ) {
        taxiRankService.deactivateTaxiRank(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Taxi rank deactivated successfully"
                )
        );
    }
}
