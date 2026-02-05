package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {
    // Removed @NotBlank to allow these to be null during updates
    private String productId;
    private String variantId;
    private String merchantId;

    @NotBlank(message = "Comment is mandatory")
    private String comment;

    @NotNull(message = "Rating is mandatory")
    @Min(1) @Max(5)
    private Integer rating;
}