package com.example.demo.service;

import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.entity.Review;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.security.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service("mainReviewService")
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestTemplate restTemplate;
    private final HttpServletRequest httpServletRequest;

    private final String PRODUCT_SERVICE_URL = "https://product-service-jzzf.onrender.com/api/v1/products/";

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String authHeader = httpServletRequest.getHeader("Authorization");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = PRODUCT_SERVICE_URL + request.getProductId() + "?variantId=" + request.getVariantId();

            log.info("DIAGNOSTIC: Validating product at {}", url);
            restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
        } catch (Exception e) {
            log.error("DIAGNOSTIC ERROR: Product validation failed: {}", e.getMessage());
            throw new ResourceNotFoundException("Product validation failed: " + e.getMessage());
        }

        Review review = Review.builder()
                .productId(request.getProductId())
                .variantId(request.getVariantId())
                .merchantId(request.getMerchantId())
                .userId(user.getId())
                .userName(user.getUsername())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return mapToResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request) {
        log.info("DIAGNOSTIC: Attempting to update review ID: {}", reviewId);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("DIAGNOSTIC: Current user from token: {}", user.getId());

        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("DIAGNOSTIC: Review not found with ID: {}", reviewId);
                    return new ResourceNotFoundException("Review not found with id: " + reviewId);
                });

        log.info("DIAGNOSTIC: Existing review owner ID: {}", existing.getUserId());
        if (!existing.getUserId().equals(user.getId())) {
            log.warn("DIAGNOSTIC: Unauthorized update attempt by user {} on review {}", user.getId(), reviewId);
            throw new UnauthorizedException("You can only update your own reviews");
        }

        try {
            existing.setComment(request.getComment());
            existing.setRating(request.getRating());

            // Fixed variable name from 'review' to 'existing'
            Review savedReview = reviewRepository.save(existing);
            log.info("DIAGNOSTIC: Review {} updated successfully in database", reviewId);
            return mapToResponse(savedReview);
        } catch (Exception e) {
            log.error("DIAGNOSTIC ERROR: Database update failed for review {}: {}", reviewId, e.getMessage());
            throw new RuntimeException("Update failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForProduct(String productId, String merchantId) {
        log.info("DIAGNOSTIC: Fetching reviews for Product: {} Merchant: {}", productId, merchantId);
        try {
            List<Review> reviews = reviewRepository.findByProductIdAndMerchantIdOrderByCreatedAtDesc(productId, merchantId);
            return reviews.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("DIAGNOSTIC ERROR: Failed to fetch reviews: {}", e.getMessage());
            throw new RuntimeException("Error fetching reviews: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteReview(Long reviewId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUserId().equals(user.getId())) {
            throw new UnauthorizedException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
        return true;
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .variantId(review.getVariantId())
                .merchantId(review.getMerchantId())
                .userId(review.getUserId())
                .userName(review.getUserName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}