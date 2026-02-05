package com.example.demo.controller;

import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.service.ReviewService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j; // Added
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Slf4j // ✅ Added logging annotation
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/create")
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest request) {
        log.info("DIAGNOSTIC: Review creation request received for product: {}", request.getProductId());
        return ResponseEntity.ok(reviewService.createReview(request));
    }

    @GetMapping("/view")
    public ResponseEntity<List<ReviewResponse>> view(
            @RequestParam String productId,
            @RequestParam String merchantId) {
        log.info("DIAGNOSTIC: Fetching reviews for Product: {} and Merchant: {}", productId, merchantId);
        return ResponseEntity.ok(reviewService.getReviewsForProduct(productId, merchantId));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        log.info("DIAGNOSTIC: Delete request received for Review ID: {}", id);
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Review deleted successfully");
    }
    @PutMapping("/update/{id}") // 1. Must be @PutMapping, NOT @PostMapping or @GetMapping
    public ResponseEntity<ReviewResponse> update(
            @PathVariable Long id, // 2. Must match the {id} in the path
            @Valid @RequestBody ReviewRequest request // 3. Must be @RequestBody
    ) {
        log.info("DIAGNOSTIC: Update request received for Review ID: {}", id);
        return ResponseEntity.ok(reviewService.updateReview(id, request));
    }
}