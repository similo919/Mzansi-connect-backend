package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.rank.TaxiRankCreateRequest;
import com.mzansiconnect.backend.dto.rank.TaxiRankResponse;
import com.mzansiconnect.backend.dto.rank.TaxiRankUpdateRequest;
import com.mzansiconnect.backend.enums.RankType;
import org.springframework.data.domain.Page;

public interface TaxiRankService {

    TaxiRankResponse createTaxiRank(
            TaxiRankCreateRequest request
    );

    TaxiRankResponse getTaxiRankById(Long id);

    Page<TaxiRankResponse> getTaxiRanks(
            String search,
            RankType rankType,
            Long locatedInAreaId,
            Boolean formalRank,
            Boolean locallyVerified,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    TaxiRankResponse updateTaxiRank(
            Long id,
            TaxiRankUpdateRequest request
    );

    void deactivateTaxiRank(Long id);
}
