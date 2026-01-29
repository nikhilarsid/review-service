package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Finds all reviews associated with a specific Product ID.
     * Use this for the Product Detail page.
     */
    List<Review> findByProductId(String productId);

    /**
     * Finds all reviews written by a specific User.
     */
    List<Review> findByUserId(Long userId);
}