package com.mzansiconnect.backend.entity;

import com.mzansiconnect.backend.enums.AssignmentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "area_rank_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaRankAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "area_id",
            nullable = false
    )
    private Area area;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "taxi_rank_id",
            nullable = false
    )
    private TaxiRank taxiRank;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "assignment_type",
            nullable = false,
            columnDefinition =
                    "enum('PRIMARY','SECONDARY','INHERITED','NEAREST_MALL_FALLBACK','NEAREST_MAJOR_FALLBACK')"
    )
    @Builder.Default
    private AssignmentType assignmentType =
            AssignmentType.PRIMARY;

    @Column(
            name = "priority",
            nullable = false
    )
    @Builder.Default
    private Integer priority = 1;

    @Column(
            name = "walking_notes",
            length = 500
    )
    private String walkingNotes;

    @Column(
            name = "assignment_reason",
            length = 500
    )
    private String assignmentReason;

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
}
