package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Existing: Fetch specifically for a merchant
    List<Review> findByProductIdAndMerchantIdOrderByCreatedAtDesc(String productId, String merchantId);

    // ✅ NEW: Fetch all reviews for a product (ignoring merchant)
    List<Review> findByProductIdOrderByCreatedAtDesc(String productId);
}