package com.example.demo.service;

import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.entity.Review;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.security.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service("mainReviewService")
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestTemplate restTemplate;

    // ✅ FIXED: Points to your live Product Service on Render
    private final String PRODUCT_SERVICE_URL = "https://product-service-jzzf.onrender.com/api/v1/products/";

    @Override
    public ReviewResponse addReview(ReviewRequest request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String targetUrl = PRODUCT_SERVICE_URL + request.getProductId();
        System.out.println("🔍 Attempting to call: " + targetUrl);

        try {
            // Validate Product Exists
            restTemplate.getForObject(targetUrl, Object.class);
            System.out.println("✅ Product found!");

        } catch (HttpClientErrorException e) {
            // This handles 400, 401, 403, 404
            System.err.println("❌ Client Error: " + e.getStatusCode());
            System.err.println("❌ Response Body: " + e.getResponseBodyAsString());

            if (e.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Product not found (404) at URL: " + targetUrl);
            } else if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                throw new RuntimeException("Review Service is not authorized to call Product Service. Is the Product API protected?");
            }
            throw e;

        } catch (HttpServerErrorException e) {
            // This handles 500 errors
            System.err.println("❌ Server Error: " + e.getStatusCode());
            System.err.println("❌ Response Body: " + e.getResponseBodyAsString());
            throw new RuntimeException("Product Service crashed (500)");

        } catch (ResourceAccessException e) {
            // This handles Connection Refused / Timeouts
            System.err.println("❌ Network Error: " + e.getMessage());
            throw new RuntimeException("Could not connect to Product Service. Check internet/URL.");

        } catch (Exception e) {
            // Fallback for anything else
            System.err.println("❌ Unknown Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unknown error calling product service");
        }

        // Proceed to save if validation passed
        Review review = Review.builder()
                .productId(request.getProductId())
                .userId(user.getId())
                .userName(user.getUsername())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        return mapToResponse(saved);
    }

    @Override
    public List<ReviewResponse> getProductReviews(String productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
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
        ReviewResponse response = new ReviewResponse();
        BeanUtils.copyProperties(review, response);
        return response;
    }
}