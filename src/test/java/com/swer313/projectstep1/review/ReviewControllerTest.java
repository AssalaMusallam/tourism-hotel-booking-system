package com.swer313.projectstep1.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swer313.projectstep1.catalog.room.PagedResponse;
import com.swer313.projectstep1.security.JwtAuthFilter;
import com.swer313.projectstep1.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ReviewService reviewService;

	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	@MockBean
	private JwtService jwtService;

	@MockBean
	private UserDetailsService userDetailsService;

	@Test
	void createReview_returnsCreated() throws Exception {
		ReviewRequestDTO req = new ReviewRequestDTO();
		req.setBookingId(1L);
		req.setGuestEmail("g@e.com");
		req.setRating(5);
		req.setComment("Excellent stay");

		ReviewResponseDTO response = new ReviewResponseDTO(
				10L,
				1L,
				2L,
				"Pink Hotel",
				"Guest One",
				"g@e.com",
				5,
				"Excellent stay",
				LocalDateTime.now(),
				true,
				LocalDate.now().plusDays(30)
		);

		when(reviewService.createReview(any())).thenReturn(response);

		mockMvc.perform(post("/api/reviews")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isCreated());
	}

	@Test
	void getHotelReviews_returnsPaged() throws Exception {
		ReviewResponseDTO review = new ReviewResponseDTO(
				11L,
				1L,
				1L,
				"Pink Hotel",
				"Guest One",
				"g@e.com",
				5,
				"Amazing",
				LocalDateTime.now(),
				true,
				LocalDate.now().plusDays(30)
		);

		Page<ReviewResponseDTO> page = new PageImpl<>(
				List.of(review),
				PageRequest.of(0, 10),
				1
		);

		PagedResponse<ReviewResponseDTO> response = PagedResponse.from(page, page.getContent());

		when(reviewService.getHotelReviews(eq(1L), any())).thenReturn(response);

		mockMvc.perform(get("/api/hotels/1/reviews"))
				.andExpect(status().isOk());
	}

	@Test
	void getRatingSummary_returnsOk() throws Exception {
		RatingSummaryDTO summary = new RatingSummaryDTO(
				2L,
				"Pink Hotel",
				4.8,
				10L,
				8L,
				1L,
				1L,
				0L,
				0L,
				80.0,
				10.0,
				10.0,
				0.0,
				0.0
		);

		when(reviewService.getRatingSummary(2L)).thenReturn(summary);

		mockMvc.perform(get("/api/hotels/2/reviews/summary"))
				.andExpect(status().isOk());
	}
}