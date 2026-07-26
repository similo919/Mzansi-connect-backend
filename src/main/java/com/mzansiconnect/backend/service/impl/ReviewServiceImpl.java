package com.mzansiconnect.backend.service.impl;

import com.mzansiconnect.backend.dto.review.ReviewCreateRequest;
import com.mzansiconnect.backend.dto.review.ReviewResponse;
import com.mzansiconnect.backend.dto.review.ReviewUpdateRequest;
import com.mzansiconnect.backend.entity.Review;
import com.mzansiconnect.backend.entity.Route;
import com.mzansiconnect.backend.entity.User;
import com.mzansiconnect.backend.enums.ReviewStatus;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.exception.DuplicateResourceException;
import com.mzansiconnect.backend.exception.ResourceNotFoundException;
import com.mzansiconnect.backend.exception.UnauthorizedOperationException;
import com.mzansiconnect.backend.mapper.RouteMapper;
import com.mzansiconnect.backend.mapper.UserMapper;
import com.mzansiconnect.backend.repository.ReviewRepository;
import com.mzansiconnect.backend.repository.RouteRepository;
import com.mzansiconnect.backend.repository.UserRepository;
import com.mzansiconnect.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final UserMapper userMapper;
    private final RouteMapper routeMapper;

    @Override
    public ReviewResponse createReview(
            String email,
            ReviewCreateRequest request
    ) {
        User user = getCurrentUser(email);
        Route route = getActiveRoute(request.getRouteId());

        if (reviewRepository.existsByUser_IdAndRoute_IdAndActiveTrue(
                user.getId(),
                route.getId()
        )) {
            throw new DuplicateResourceException(
                    "You have already reviewed this route"
            );
        }

        Review review = Review.builder()
                .user(user)
                .route(route)
                .rating(request.getRating())
                .comment(normalizeComment(request.getComment()))
                .status(ReviewStatus.PENDING)
                .active(true)
                .build();

        return toResponse(
                reviewRepository.save(review),
                user.getEmail()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getApprovedRouteReviews(
            Long routeId,
            String currentUserEmail
    ) {
        getActiveRoute(routeId);

        return reviewRepository
                .findByRoute_IdAndStatusAndActiveTrueOrderByCreatedAtDesc(
                        routeId,
                        ReviewStatus.APPROVED
                )
                .stream()
                .map(review -> toResponse(review, currentUserEmail))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getCurrentUserReviews(String email) {
        User user = getCurrentUser(email);

        return reviewRepository
                .findByUser_IdAndActiveTrueOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(review -> toResponse(review, user.getEmail()))
                .toList();
    }

    @Override
    public ReviewResponse updateReview(
            String email,
            Long reviewId,
            ReviewUpdateRequest request
    ) {
        User user = getCurrentUser(email);
        Review review = getReview(reviewId);
        requireOwner(review, user);

        review.setRating(request.getRating());
        review.setComment(normalizeComment(request.getComment()));
        review.setStatus(ReviewStatus.PENDING);

        return toResponse(review, user.getEmail());
    }

    @Override
    public void deleteReview(
            String email,
            Long reviewId
    ) {
        User user = getCurrentUser(email);
        Review review = getReview(reviewId);
        requireOwner(review, user);

        review.setActive(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForAdmin(
            ReviewStatus status
    ) {
        ReviewStatus resolvedStatus =
                status == null ? ReviewStatus.PENDING : status;

        return reviewRepository
                .findByStatusAndActiveTrueOrderByCreatedAtDesc(
                        resolvedStatus
                )
                .stream()
                .map(review -> toResponse(review, null))
                .toList();
    }

    @Override
    public ReviewResponse updateReviewStatus(
            Long reviewId,
            ReviewStatus status
    ) {
        Review review = getReview(reviewId);
        review.setStatus(status);

        if (status == ReviewStatus.REJECTED) {
            review.setActive(false);
        }

        return toResponse(review, null);
    }

    private ReviewResponse toResponse(
            Review review,
            String currentUserEmail
    ) {
        boolean mine = currentUserEmail != null
                && review.getUser() != null
                && review.getUser().getEmail().equalsIgnoreCase(
                        currentUserEmail
                );

        return ReviewResponse.builder()
                .id(review.getId())
                .user(userMapper.toResponse(review.getUser()))
                .route(routeMapper.toResponse(review.getRoute()))
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus())
                .active(review.getActive())
                .mine(mine)
                .canEdit(mine)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private User getCurrentUser(String email) {
        return userRepository
                .findByEmailIgnoreCaseAndEnabledTrue(email)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Authenticated user was not found"
                        )
                );
    }

    private Route getActiveRoute(Long routeId) {
        return routeRepository
                .findByIdAndActiveTrue(routeId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Route was not found"
                        )
                );
    }

    private Review getReview(Long reviewId) {
        return reviewRepository
                .findById(reviewId)
                .filter(review -> Boolean.TRUE.equals(review.getActive()))
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Review was not found"
                        )
                );
    }

    private void requireOwner(
            Review review,
            User user
    ) {
        if (!review.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedOperationException(
                    "You can only change your own reviews"
            );
        }
    }

    private String normalizeComment(String comment) {
        String normalized = comment == null ? null : comment.trim();

        if (normalized == null || normalized.isBlank()) {
            throw new BusinessValidationException(
                    "Comment is required"
            );
        }

        return normalized;
    }
}