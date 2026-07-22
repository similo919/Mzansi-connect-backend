package com.mzansiconnect.backend.entity;

import com.mzansiconnect.backend.enums.RouteType;
import com.mzansiconnect.backend.enums.VerificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "routes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_route_code",
                        columnNames = "route_code"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(
            name = "route_code",
            nullable = false,
            unique = true,
            length = 60
    )
    private String routeCode;

    @Column(
            name = "route_name",
            nullable = false,
            length = 200
    )
    private String routeName;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "starting_area_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_route_starting_area"
            )
    )
    private Area startingArea;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "destination_area_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_route_destination_area"
            )
    )
    private Area destinationArea;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "departure_rank_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_route_departure_rank"
            )
    )
    private TaxiRank departureRank;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "arrival_rank_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_route_arrival_rank"
            )
    )
    private TaxiRank arrivalRank;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "route_type",
            nullable = false,
            columnDefinition =
                    "enum('DIRECT','CONNECTING')"
    )
    @Builder.Default
    private RouteType routeType = RouteType.DIRECT;

    @Column(
            name = "taxi_sign",
            length = 150
    )
    private String taxiSign;

    @Column(
            name = "boarding_instructions",
            length = 1000
    )
    private String boardingInstructions;

    @Column(
            name = "drop_off_instructions",
            length = 1000
    )
    private String dropOffInstructions;

    @Column(
            name = "travel_notes",
            length = 1000
    )
    private String travelNotes;

    @Column(
            name = "estimated_duration_minutes",
            nullable = false
    )
    private Integer estimatedDurationMinutes;

    @Column(
            name = "estimated_waiting_minutes",
            nullable = false
    )
    @Builder.Default
    private Integer estimatedWaitingMinutes = 0;

    @Column(
            name = "operates_weekdays",
            nullable = false
    )
    @Builder.Default
    private Boolean operatesWeekdays = true;

    @Column(
            name = "operates_weekends",
            nullable = false
    )
    @Builder.Default
    private Boolean operatesWeekends = true;

    @Column(
            name = "operating_hours",
            length = 150
    )
    private String operatingHours;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "verification_status",
            nullable = false,
            columnDefinition =
                    "enum('UNVERIFIED','COMMUNITY_VERIFIED','RANK_VERIFIED','ADMIN_VERIFIED')"
    )
    @Builder.Default
    private VerificationStatus verificationStatus =
            VerificationStatus.UNVERIFIED;

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

    @Column(
            name = "active",
            nullable = false
    )
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;
}
