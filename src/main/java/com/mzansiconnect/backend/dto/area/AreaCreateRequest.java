package com.mzansiconnect.backend.dto.area;

import com.mzansiconnect.backend.enums.AreaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaCreateRequest {

    @NotBlank(message = "Area name is required")
    @Size(
            max = 120,
            message = "Area name cannot exceed 120 characters"
    )
    private String name;

    @NotNull(message = "Area type is required")
    private AreaType areaType;

    private Long parentAreaId;

    @NotBlank(message = "Province is required")
    @Size(
            max = 100,
            message = "Province cannot exceed 100 characters"
    )
    private String province;

    @NotBlank(message = "Municipality is required")
    @Size(
            max = 150,
            message = "Municipality cannot exceed 150 characters"
    )
    private String municipality;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    private String description;
}
