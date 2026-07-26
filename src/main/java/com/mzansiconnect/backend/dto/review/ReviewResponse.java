package com.mzansiconnect.backend.dto.review;

import com.mzansiconnect.backend.dto.auth.UserResponse;
import com.mzansiconnect.backend.dto.route.RouteResponse;
import com.mzansiconnect.backend.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private UserResponse user;
    private RouteResponse route;
    private Integer rating;
    private String comment;
    private ReviewStatus status;
    private Boolean active;
    private Boolean mine;
    private Boolean canEdit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}