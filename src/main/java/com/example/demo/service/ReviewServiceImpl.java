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

        // 1. Validate Product Existence
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Build URL: Base product ID is required
            String url = PRODUCT_SERVICE_URL + request.getProductId();

            // Append variantId only if it is provided
            if (request.getVariantId() != null && !request.getVariantId().isEmpty()) {
                url += "?variantId=" + request.getVariantId();
            }

            log.info("DIAGNOSTIC: Validating product at {}", url);
            restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
        } catch (Exception e) {
            log.error("DIAGNOSTIC ERROR: Product validation failed: {}", e.getMessage());
            throw new ResourceNotFoundException("Product validation failed: " + e.getMessage());
        }

        // 2. Build Review (Allowing nulls for merchant/variant)
        Review review = Review.builder()
                .productId(request.getProductId())
                .variantId(request.getVariantId())  // Can be null
                .merchantId(request.getMerchantId()) // Can be null
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

        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!existing.getUserId().equals(user.getId())) {
            throw new UnauthorizedException("You can only update your own reviews");
        }

        try {
            existing.setComment(request.getComment());
            existing.setRating(request.getRating());
            return mapToResponse(reviewRepository.save(existing));
        } catch (Exception e) {
            throw new RuntimeException("Update failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForProduct(String productId, String merchantId) {
        // Keeps old functionality for strict filtering
        log.info("DIAGNOSTIC: Fetching reviews for Product: {} Merchant: {}", productId, merchantId);
        try {
            return reviewRepository.findByProductIdAndMerchantIdOrderByCreatedAtDesc(productId, merchantId)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching reviews: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviewsByProductId(String productId) {
        // Primary method for fetching product-centric reviews
        log.info("DIAGNOSTIC: Fetching ALL reviews for Product ID: {}", productId);
        try {
            return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("DIAGNOSTIC ERROR: Failed to fetch all reviews: {}", e.getMessage());
            throw new RuntimeException("Error fetching product reviews");
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