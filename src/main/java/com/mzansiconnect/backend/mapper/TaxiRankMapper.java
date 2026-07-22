package com.mzansiconnect.backend.mapper;

import com.mzansiconnect.backend.dto.area.AreaSummaryResponse;
import com.mzansiconnect.backend.dto.rank.TaxiRankCreateRequest;
import com.mzansiconnect.backend.dto.rank.TaxiRankResponse;
import com.mzansiconnect.backend.dto.rank.TaxiRankSummaryResponse;
import com.mzansiconnect.backend.dto.rank.TaxiRankUpdateRequest;
import com.mzansiconnect.backend.entity.Area;
import com.mzansiconnect.backend.entity.TaxiRank;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TaxiRankMapper {

    public TaxiRank toEntity(
            TaxiRankCreateRequest request,
            Area locatedInArea
    ) {
        boolean locallyVerified =
                Boolean.TRUE.equals(
                        request.getLocallyVerified()
                );

        return TaxiRank.builder()
                .name(normalizeRequiredText(request.getName()))
                .rankType(request.getRankType())
                .locatedInArea(locatedInArea)
                .addressDescription(
                        normalizeOptionalText(
                                request.getAddressDescription()
                        )
                )
                .operatingHours(
                        normalizeOptionalText(
                                request.getOperatingHours()
                        )
                )
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .formalRank(
                        request.getFormalRank() == null
                                || request.getFormalRank()
                )
                .active(true)
                .locallyVerified(locallyVerified)
                .lastVerifiedAt(
                        locallyVerified
                                ? LocalDateTime.now()
                                : null
                )
                .verificationNotes(
                        normalizeOptionalText(
                                request.getVerificationNotes()
                        )
                )
                .build();
    }

    public void updateEntity(
            TaxiRank taxiRank,
            TaxiRankUpdateRequest request,
            Area locatedInArea
    ) {
        taxiRank.setName(
                normalizeRequiredText(request.getName())
        );

        taxiRank.setRankType(request.getRankType());
        taxiRank.setLocatedInArea(locatedInArea);

        taxiRank.setAddressDescription(
                normalizeOptionalText(
                        request.getAddressDescription()
                )
        );

        taxiRank.setOperatingHours(
                normalizeOptionalText(
                        request.getOperatingHours()
                )
        );

        taxiRank.setLatitude(request.getLatitude());
        taxiRank.setLongitude(request.getLongitude());

        if (request.getFormalRank() != null) {
            taxiRank.setFormalRank(
                    request.getFormalRank()
            );
        }

        if (request.getLocallyVerified() != null) {
            updateVerificationState(
                    taxiRank,
                    request.getLocallyVerified()
            );
        }

        taxiRank.setVerificationNotes(
                normalizeOptionalText(
                        request.getVerificationNotes()
                )
        );
    }

    public TaxiRankResponse toResponse(
            TaxiRank taxiRank
    ) {
        return TaxiRankResponse.builder()
                .id(taxiRank.getId())
                .name(taxiRank.getName())
                .rankType(taxiRank.getRankType())
                .locatedInArea(
                        toAreaSummary(
                                taxiRank.getLocatedInArea()
                        )
                )
                .addressDescription(
                        taxiRank.getAddressDescription()
                )
                .operatingHours(
                        taxiRank.getOperatingHours()
                )
                .latitude(taxiRank.getLatitude())
                .longitude(taxiRank.getLongitude())
                .formalRank(taxiRank.getFormalRank())
                .active(taxiRank.getActive())
                .locallyVerified(
                        taxiRank.getLocallyVerified()
                )
                .lastVerifiedAt(
                        taxiRank.getLastVerifiedAt()
                )
                .verificationNotes(
                        taxiRank.getVerificationNotes()
                )
                .build();
    }

    public TaxiRankSummaryResponse toSummary(
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

    private void updateVerificationState(
            TaxiRank taxiRank,
            Boolean locallyVerified
    ) {
        if (Boolean.TRUE.equals(locallyVerified)) {
            taxiRank.setLocallyVerified(true);

            if (taxiRank.getLastVerifiedAt() == null) {
                taxiRank.setLastVerifiedAt(
                        LocalDateTime.now()
                );
            }
        } else {
            taxiRank.setLocallyVerified(false);
            taxiRank.setLastVerifiedAt(null);
        }
    }

    private String normalizeRequiredText(
            String value
    ) {
        return value == null
                ? null
                : value.trim();
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
