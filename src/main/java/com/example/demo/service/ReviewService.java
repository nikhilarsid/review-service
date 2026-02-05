package com.example.demo.service;

import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.dto.response.ReviewResponse;
import java.util.List;

// File: src/main/java/com/example/demo/service/ReviewService.java
public interface ReviewService {
    ReviewResponse createReview(ReviewRequest request);
    ReviewResponse updateReview(Long reviewId, ReviewRequest request);
    List<ReviewResponse> getReviewsForProduct(String productId, String merchantId);
    boolean deleteReview(Long reviewId); // Must be boolean
}