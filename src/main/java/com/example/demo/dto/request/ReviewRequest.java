package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotBlank(message = "Product ID is required")
    private String productId;

    @Min(1) @Max(5)
    private Integer rating;

    @Size(max = 500)
    private String comment;
}