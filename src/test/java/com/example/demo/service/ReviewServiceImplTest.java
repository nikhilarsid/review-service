package com.example.demo.service;

import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.entity.Review;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.security.User;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    // ---------------------------------------------------------
    // Setup authenticated principal
    // ---------------------------------------------------------
    @BeforeEach
    void setupSecurity() {

        // ⭐ Using review-service User class
        User mockUser = new User("user1", "john");

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        mockUser,
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    // ---------------------------------------------------------
    // CREATE SUCCESS
    // ---------------------------------------------------------
    @Test
    void shouldCreateReview() {

        ReviewRequest req = new ReviewRequest();
        req.setProductId("p1");
        req.setVariantId("v1");
        req.setMerchantId("m1");
        req.setComment("Nice product");
        req.setRating(5);

        when(httpServletRequest.getHeader("Authorization"))
                .thenReturn("Bearer token");

        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        Review saved = Review.builder()
                .id(1L)
                .productId("p1")
                .variantId("v1")
                .merchantId("m1")
                .userId("user1")
                .userName("john")
                .rating(5)
                .comment("Nice product")
                .build();

        when(reviewRepository.save(any()))
                .thenReturn(saved);

        ReviewResponse response = reviewService.createReview(req);

        assertThat(response.getId()).isEqualTo(1L);
        verify(reviewRepository).save(any());
    }

    // ---------------------------------------------------------
    // CREATE FAIL VALIDATION
    // ---------------------------------------------------------
    @Test
    void shouldFailCreateWhenProductInvalid() {

        ReviewRequest req = new ReviewRequest();
        req.setProductId("bad");
        req.setVariantId("v1");
        req.setMerchantId("m1");
        req.setComment("Nice");
        req.setRating(5);

        when(httpServletRequest.getHeader("Authorization"))
                .thenReturn("Bearer token");

        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenThrow(RuntimeException.class);

        assertThatThrownBy(() ->
                reviewService.createReview(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------------------------------------------------------
    // UPDATE SUCCESS
    // ---------------------------------------------------------
    @Test
    void shouldUpdateReview() {

        Review existing = Review.builder()
                .id(1L)
                .userId("user1")
                .build();

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(reviewRepository.save(any()))
                .thenReturn(existing);

        ReviewRequest req = new ReviewRequest();
        req.setComment("Updated");
        req.setRating(4);

        ReviewResponse response =
                reviewService.updateReview(1L, req);

        assertThat(response).isNotNull();
    }

    // ---------------------------------------------------------
    // UPDATE UNAUTHORIZED
    // ---------------------------------------------------------
    @Test
    void shouldRejectUpdateIfNotOwner() {

        Review existing = Review.builder()
                .id(1L)
                .userId("otherUser")
                .build();

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        ReviewRequest req = new ReviewRequest();
        req.setComment("X");
        req.setRating(3);

        assertThatThrownBy(() ->
                reviewService.updateReview(1L, req))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ---------------------------------------------------------
    // GET REVIEWS
    // ---------------------------------------------------------
    @Test
    void shouldReturnReviews() {

        Review review = Review.builder()
                .id(1L)
                .productId("p1")
                .merchantId("m1")
                .comment("Nice")
                .rating(5)
                .build();

        when(reviewRepository
                .findByProductIdAndMerchantIdOrderByCreatedAtDesc("p1","m1"))
                .thenReturn(List.of(review));

        List<ReviewResponse> result =
                reviewService.getReviewsForProduct("p1","m1");

        assertThat(result).hasSize(1);
    }

    // ---------------------------------------------------------
    // DELETE SUCCESS
    // ---------------------------------------------------------
    @Test
    void shouldDeleteReview() {

        Review review = Review.builder()
                .id(1L)
                .userId("user1")
                .build();

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(review));

        boolean result = reviewService.deleteReview(1L);

        assertThat(result).isTrue();
        verify(reviewRepository).delete(review);
    }

    // ---------------------------------------------------------
    // DELETE UNAUTHORIZED
    // ---------------------------------------------------------
    @Test
    void shouldRejectDelete() {

        Review review = Review.builder()
                .id(1L)
                .userId("otherUser")
                .build();

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(review));

        assertThatThrownBy(() ->
                reviewService.deleteReview(1L))
                .isInstanceOf(UnauthorizedException.class);
    }
}
