package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.rank.ResolvedTaxiRankResponse;

public interface TaxiRankResolutionService {

    ResolvedTaxiRankResponse resolveTaxiRank(Long areaId);
}
