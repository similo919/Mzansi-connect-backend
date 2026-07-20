package com.mzansiconnect.backend.entity;

import com.mzansiconnect.backend.enums.AreaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "areas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(
            name = "name",
            nullable = false,
            length = 120
    )
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "area_type",
            nullable = false,
            columnDefinition =
                    "enum('REGION','SUBURB','ZONE','EXTENSION','CITY','TOWN')"
    )
    @Builder.Default
    private AreaType areaType = AreaType.SUBURB;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_area_id")
    private Area parentArea;

    @Column(
            name = "province",
            nullable = false,
            length = 100
    )
    private String province;

    @Column(
            name = "municipality",
            nullable = false,
            length = 150
    )
    private String municipality;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    @Column(
            name = "active",
            nullable = false
    )
    @Builder.Default
    private Boolean active = true;
}
