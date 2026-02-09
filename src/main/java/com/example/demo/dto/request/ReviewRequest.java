package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotBlank(message = "Product ID is mandatory")
    private String productId;

    // Optional: Only needed if reviewing a specific variant
    private String variantId;

    // Optional: Only needed if reviewing a specific merchant
    private String merchantId;

    @NotBlank(message = "Comment is mandatory")
    private String comment;

    @NotNull(message = "Rating is mandatory")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;
}