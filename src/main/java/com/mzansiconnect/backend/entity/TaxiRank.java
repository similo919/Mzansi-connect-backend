package com.mzansiconnect.backend.entity;

import com.mzansiconnect.backend.enums.RankType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "taxi_ranks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxiRank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(
            name = "name",
            nullable = false,
            length = 180
    )
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "rank_type",
            nullable = false,
            columnDefinition =
                    "enum('TOWNSHIP_RANK','MALL_RANK','HOSPITAL_RANK','MAJOR_RANK','INFORMAL_PICKUP_POINT')"
    )
    private RankType rankType;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "located_in_area_id",
            nullable = false
    )
    private Area locatedInArea;

    @Column(
            name = "address_description",
            length = 500
    )
    private String addressDescription;

    @Column(
            name = "operating_hours",
            length = 150
    )
    private String operatingHours;

    @Column(
            name = "latitude",
            precision = 10,
            scale = 7
    )
    private BigDecimal latitude;

    @Column(
            name = "longitude",
            precision = 10,
            scale = 7
    )
    private BigDecimal longitude;

    @Column(
            name = "formal_rank",
            nullable = false
    )
    @Builder.Default
    private Boolean formalRank = true;

    @Column(
            name = "active",
            nullable = false
    )
    @Builder.Default
    private Boolean active = true;

    @Column(
            name = "locally_verified",
            nullable = false
    )
    @Builder.Default
    private Boolean locallyVerified = false;

    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    @Column(
            name = "verification_notes",
            length = 500
    )
    private String verificationNotes;
}
