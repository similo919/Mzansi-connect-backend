package com.mzansiconnect.backend.dto.rank;

import com.mzansiconnect.backend.enums.RankType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxiRankSummaryResponse {

    private Long id;
    private String name;
    private RankType rankType;
}
