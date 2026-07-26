package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.dto.review.ReviewCreateRequest;
import com.mzansiconnect.backend.dto.review.ReviewResponse;
import com.mzansiconnect.backend.dto.review.ReviewUpdateRequest;
import com.mzansiconnect.backend.response.ApiResponse;
import com.mzansiconnect.backend.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid
            @RequestBody
            ReviewCreateRequest request,

            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Review submitted for moderation",
                                reviewService.createReview(
                                        authentication.getName(),
                                        request
                                )
                        )
                );
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>>
    getApprovedRouteReviews(
            @PathVariable
            @Positive(message = "Route ID must be positive")
            Long routeId,

            Authentication authentication
    ) {
        String email = authentication == null
                ? null
                : authentication.getName();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Approved route reviews retrieved successfully",
                        reviewService.getApprovedRouteReviews(
                                routeId,
                                email
                        )
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>>
    getCurrentUserReviews(Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User reviews retrieved successfully",
                        reviewService.getCurrentUserReviews(
                                authentication.getName()
                        )
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable
            @Positive(message = "Review ID must be positive")
            Long id,

            @Valid
            @RequestBody
            ReviewUpdateRequest request,

            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Review updated successfully",
                        reviewService.updateReview(
                                authentication.getName(),
                                id,
                                request
                        )
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable
            @Positive(message = "Review ID must be positive")
            Long id,

            Authentication authentication
    ) {
        reviewService.deleteReview(
                authentication.getName(),
                id
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Review deleted successfully"
                )
        );
    }
}