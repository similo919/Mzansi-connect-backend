package com.mzansiconnect.backend.entity;

import com.mzansiconnect.backend.enums.FareType;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fares")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "route_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_fare_route"
            )
    )
    private Route route;

    @Column(
            name = "amount",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "currency",
            nullable = false,
            length = 3
    )
    @Builder.Default
    private String currency = "ZAR";

    @Enumerated(EnumType.STRING)
    @Column(
            name = "fare_type",
            nullable = false,
            columnDefinition =
                    "enum('STANDARD','PEAK','WEEKEND','HOLIDAY')"
    )
    @Builder.Default
    private FareType fareType = FareType.STANDARD;

    @Column(
            name = "effective_from",
            nullable = false
    )
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

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
