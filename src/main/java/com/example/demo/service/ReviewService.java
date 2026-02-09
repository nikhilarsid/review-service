package com.example.demo.service;

import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.dto.response.ReviewResponse;
import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest request);
    ReviewResponse updateReview(Long reviewId, ReviewRequest request);
    List<ReviewResponse> getReviewsForProduct(String productId, String merchantId);
    boolean deleteReview(Long reviewId);

    // ✅ NEW Method
    List<ReviewResponse> getAllReviewsByProductId(String productId);
}