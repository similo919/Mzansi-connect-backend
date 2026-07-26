package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.dto.review.ReviewResponse;
import com.mzansiconnect.backend.dto.review.ReviewStatusUpdateRequest;
import com.mzansiconnect.backend.enums.ReviewStatus;
import com.mzansiconnect.backend.response.ApiResponse;
import com.mzansiconnect.backend.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Validated
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
            @RequestParam(required = false)
            ReviewStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Reviews retrieved successfully",
                        reviewService.getReviewsForAdmin(status)
                )
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateStatus(
            @PathVariable
            @Positive(message = "Review ID must be positive")
            Long id,

            @Valid
            @RequestBody
            ReviewStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Review status updated successfully",
                        reviewService.updateReviewStatus(
                                id,
                                request.getStatus()
                        )
                )
        );
    }
}