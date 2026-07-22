package com.mzansiconnect.backend.dto.rank;

import com.mzansiconnect.backend.dto.area.AreaSummaryResponse;
import com.mzansiconnect.backend.enums.RankType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxiRankResponse {

    private Long id;
    private String name;
    private RankType rankType;

    private AreaSummaryResponse locatedInArea;

    private String addressDescription;
    private String operatingHours;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private Boolean formalRank;
    private Boolean active;
    private Boolean locallyVerified;

    private LocalDateTime lastVerifiedAt;
    private String verificationNotes;
}
