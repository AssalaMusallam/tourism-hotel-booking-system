package com.swer313.projectstep1.availabilitypricing.availability;

import com.swer313.projectstep1.errors.GlobalExceptionHandler;
import com.swer313.projectstep1.security.JwtAuthFilter;
import com.swer313.projectstep1.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvailabilityController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AvailabilityControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AvailabilityService availabilityService;

	// Mock security beans that may be required by the test context
	@MockBean
	private JwtService jwtService;

	@MockBean
	private UserDetailsService userDetailsService;

	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	@Test
	@DisplayName("checkAvailability valid request returns 200 and expected JSON")
	void checkAvailability_validRequest_returns200_andJson() throws Exception {
		AvailabilityResponseDto dto = new AvailabilityResponseDto(
				1L, "Hotel", 2L, "Room", LocalDate.of(2026,4,10), LocalDate.of(2026,4,12),
				2, 2, 3, 5, 1, 4, true, new BigDecimal("100.00"), null
		);

		when(availabilityService.checkAvailability(eq(2L), any(LocalDate.class), any(LocalDate.class), any()))
				.thenReturn(dto);

		mockMvc.perform(get("/api/v1/availability")
						.param("roomTypeId", "2")
						.param("checkIn", "2026-04-10")
						.param("checkOut", "2026-04-12")
						.param("guests", "2")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.hotelId", is(1)))
				.andExpect(jsonPath("$.roomTypeId", is(2)))
				.andExpect(jsonPath("$.available", is(true)));
	}

	@Test
	@DisplayName("checkAvailability missing params returns 400")
	void checkAvailability_missingParams_returns400() throws Exception {
		mockMvc.perform(get("/api/v1/availability")
						.param("roomTypeId", "2")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message",
						containsString("Required request parameter 'checkIn'")));
	}

	@Test
	@DisplayName("checkHotelAvailability valid request returns 200 and paged JSON")
	void checkHotelAvailability_validRequest_returns200_andPagedJson() throws Exception {
		AvailabilitySummaryDto s = new AvailabilitySummaryDto(
				1L, "Hotel", 10L, "Room", 2, 5, 2L, 3L, true, null, new BigDecimal("120.00")
		);

		PageImpl<AvailabilitySummaryDto> page = new PageImpl<>(List.of(s), PageRequest.of(0,10), 1);
		PagedResponse<AvailabilitySummaryDto> resp = PagedResponse.from(page, List.of(s));

		when(availabilityService.checkHotelAvailability(eq(1L), any(LocalDate.class), any(LocalDate.class), any(), any(), any(), any()))
				.thenReturn(resp);

		mockMvc.perform(get("/api/v1/hotels/1/availability")
						.param("checkIn", "2026-04-10")
						.param("checkOut", "2026-04-12")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()", is(1)))
				.andExpect(jsonPath("$.content[0].roomTypeId", is(10)));
	}

	@Test
	@DisplayName("checkHotelAvailability invalid page/size/sort params return 400 with messages")
	void checkHotelAvailability_invalidParams_return400() throws Exception {
		// invalid page
		mockMvc.perform(get("/api/v1/hotels/1/availability")
						.param("checkIn", "2026-04-10")
						.param("checkOut", "2026-04-12")
						.param("page", "-1"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("page must be >= 0")));

		// invalid size
		mockMvc.perform(get("/api/v1/hotels/1/availability")
						.param("checkIn", "2026-04-10")
						.param("checkOut", "2026-04-12")
						.param("size", "0"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("size must be > 0")));

		// invalid sort field
		mockMvc.perform(get("/api/v1/hotels/1/availability")
						.param("checkIn", "2026-04-10")
						.param("checkOut", "2026-04-12")
						.param("sort", "unknown,asc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("Invalid sort field")));

		// invalid sort direction
		mockMvc.perform(get("/api/v1/hotels/1/availability")
						.param("checkIn", "2026-04-10")
						.param("checkOut", "2026-04-12")
						.param("sort", "id,invalid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("Invalid sort direction")));
	}
}