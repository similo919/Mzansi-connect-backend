package com.mzansiconnect.backend.dto.review;

import com.mzansiconnect.backend.enums.ReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewStatusUpdateRequest {

    @NotNull(message = "Review status is required")
    private ReviewStatus status;
}