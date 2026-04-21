package com.swer313.projectstep1.review;

import com.swer313.projectstep1.catalog.room.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewResponseDTO createReview(ReviewRequestDTO dto);

    PagedResponse<ReviewResponseDTO> getHotelReviews(Long hotelId, Pageable pageable);

    RatingSummaryDTO getRatingSummary(Long hotelId);
}