package com.example.demo.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder // ✅ Added for manual mapping
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private String productId;
    private String variantId;
    private String merchantId;
    private String userId; // ✅ Ensure this is String
    private String userName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}