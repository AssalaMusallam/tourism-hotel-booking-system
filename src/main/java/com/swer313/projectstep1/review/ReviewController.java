package com.swer313.projectstep1.review;

import com.swer313.projectstep1.catalog.room.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Reviews", description = "Hotel reviews and ratings")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // POST /api/reviews — إضافة review جديد
    @PostMapping("/reviews")
    @Operation(summary = "Submit a review for a completed booking")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @RequestBody ReviewRequestDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewService.createReview(dto));
    }

    // GET /api/hotels/{hotelId}/reviews — جلب reviews فندق معيّن
    @GetMapping("/hotels/{hotelId}/reviews")
    @Operation(summary = "Get all reviews for a hotel")
    public ResponseEntity<PagedResponse<ReviewResponseDTO>> getHotelReviews(
            @PathVariable Long hotelId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(
                reviewService.getHotelReviews(hotelId, pageable)
        );
    }

    // GET /api/hotels/{hotelId}/reviews/summary — rating summary
    @GetMapping("/hotels/{hotelId}/reviews/summary")
    @Operation(summary = "Get rating summary for a hotel")
    public ResponseEntity<RatingSummaryDTO> getRatingSummary(
            @PathVariable Long hotelId) {

        return ResponseEntity.ok(
                reviewService.getRatingSummary(hotelId)
        );
    }
}
