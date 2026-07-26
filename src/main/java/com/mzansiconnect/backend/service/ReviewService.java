package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.review.ReviewCreateRequest;
import com.mzansiconnect.backend.dto.review.ReviewResponse;
import com.mzansiconnect.backend.dto.review.ReviewUpdateRequest;
import com.mzansiconnect.backend.enums.ReviewStatus;

import java.util.List;

public interface ReviewService {

    ReviewResponse createReview(String email, ReviewCreateRequest request);

    List<ReviewResponse> getApprovedRouteReviews(
            Long routeId,
            String currentUserEmail
    );

    List<ReviewResponse> getCurrentUserReviews(String email);

    ReviewResponse updateReview(
            String email,
            Long reviewId,
            ReviewUpdateRequest request
    );

    void deleteReview(String email, Long reviewId);

    List<ReviewResponse> getReviewsForAdmin(ReviewStatus status);

    ReviewResponse updateReviewStatus(
            Long reviewId,
            ReviewStatus status
    );
}