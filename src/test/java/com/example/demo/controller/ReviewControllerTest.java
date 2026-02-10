package com.example.demo.controller;

import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.service.ReviewService;
import com.example.demo.security.JwtAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------- MOCKS ----------
    @MockBean
    private ReviewService reviewService;

    // Security mocks (required for context load)
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // =========================================================
    // CREATE
    // =========================================================
    @Test
    void shouldCreateReview() throws Exception {

        ReviewRequest req = new ReviewRequest();
        req.setProductId("p1");
        req.setVariantId("v1");
        req.setMerchantId("m1");
        req.setComment("Great product!");
        req.setRating(5);

        ReviewResponse res = ReviewResponse.builder()
                .id(1L)
                .productId("p1")
                .variantId("v1")
                .merchantId("m1")
                .userId("u1")
                .userName("John")
                .rating(5)
                .comment("Great product!")
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.createReview(any()))
                .thenReturn(res);

        mockMvc.perform(post("/api/v1/reviews/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productId").value("p1"));

        verify(reviewService).createReview(any());
    }

    // =========================================================
    // VIEW
    // =========================================================
    @Test
    void shouldReturnReviews() throws Exception {

        ReviewResponse res = ReviewResponse.builder()
                .id(1L)
                .productId("p1")
                .merchantId("m1")
                .comment("Nice")
                .rating(4)
                .build();

        when(reviewService.getReviewsForProduct("p1", "m1"))
                .thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/reviews/view")
                        .param("productId", "p1")
                        .param("merchantId", "m1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(reviewService)
                .getReviewsForProduct("p1", "m1");
    }

    // =========================================================
    // DELETE
    // =========================================================
    @Test
    void shouldDeleteReview() throws Exception {

        mockMvc.perform(delete("/api/v1/reviews/delete/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Review deleted successfully"));

        verify(reviewService).deleteReview(5L);
    }

    // =========================================================
    // UPDATE
    // =========================================================
    @Test
    void shouldUpdateReview() throws Exception {

        ReviewRequest req = new ReviewRequest();
        req.setProductId("p1");
        req.setVariantId("v1");
        req.setMerchantId("m1");
        req.setComment("Updated review");
        req.setRating(4);

        ReviewResponse res = ReviewResponse.builder()
                .id(10L)
                .productId("p1")
                .merchantId("m1")
                .comment("Updated review")
                .rating(4)
                .build();

        when(reviewService.updateReview(eq(10L), any()))
                .thenReturn(res);

        mockMvc.perform(put("/api/v1/reviews/update/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(reviewService)
                .updateReview(eq(10L), any());
    }
}
