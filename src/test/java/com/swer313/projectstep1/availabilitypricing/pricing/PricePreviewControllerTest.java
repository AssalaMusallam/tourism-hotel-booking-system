package com.swer313.projectstep1.availabilitypricing.pricing;

import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import com.swer313.projectstep1.catalog.room.RoomTypeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PricePreviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class PricePreviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RoomTypeRepository roomTypeRepository;

	@MockBean
	private PricingCalculator pricingCalculator;

	// مطلوب فقط حتى يفتح الـ ApplicationContext
	@MockBean
	private com.swer313.projectstep1.security.JwtService jwtService;

	@Test
	void getPricePreview_roomExistsAndActive_returns200() throws Exception {
		RoomType roomType = new RoomType();
		roomType.setBasePrice(new BigDecimal("120.00"));
		roomType.setStatus(RoomTypeStatus.ACTIVE);

		when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
		when(pricingCalculator.calculateBreakdown(any(), any(), any()))
				.thenReturn(new PriceBreakdownDTO(
						new BigDecimal("120.00"),
						1,
						java.util.List.of(),
						new BigDecimal("120.00"),
						new BigDecimal("0.16"),
						new BigDecimal("19.20"),
						new BigDecimal("139.20")
				));

		mockMvc.perform(get("/api/room-types/1/price-preview")
						.param("checkIn", "2026-06-01")
						.param("checkOut", "2026-06-02"))
				.andExpect(status().isOk());

		verify(roomTypeRepository).findById(1L);
		verify(pricingCalculator).calculateBreakdown(any(), any(), any());
	}

	@Test
	void getPricePreview_checkOutNotAfterCheckIn_returns400() throws Exception {
		mockMvc.perform(get("/api/room-types/1/price-preview")
						.param("checkIn", "2026-06-02")
						.param("checkOut", "2026-06-02"))
				.andExpect(status().isBadRequest());

		verify(roomTypeRepository, never()).findById(anyLong());
		verify(pricingCalculator, never()).calculateBreakdown(any(), any(), any());
	}

	@Test
	void getPricePreview_stayExceedsMaxNights_returns400() throws Exception {
		mockMvc.perform(get("/api/room-types/1/price-preview")
						.param("checkIn", "2025-01-01")
						.param("checkOut", "2026-01-02"))
				.andExpect(status().isBadRequest());

		verify(roomTypeRepository, never()).findById(anyLong());
		verify(pricingCalculator, never()).calculateBreakdown(any(), any(), any());
	}

	@Test
	void getPricePreview_roomTypeNotFound_returns404() throws Exception {
		when(roomTypeRepository.findById(anyLong())).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/room-types/1/price-preview")
						.param("checkIn", "2026-06-01")
						.param("checkOut", "2026-06-02"))
				.andExpect(status().isNotFound());

		verify(roomTypeRepository).findById(1L);
		verify(pricingCalculator, never()).calculateBreakdown(any(), any(), any());
	}

	@Test
	void getPricePreview_roomTypeNotActive_returns400() throws Exception {
		RoomType roomType = new RoomType();
		roomType.setBasePrice(new BigDecimal("100.00"));
		roomType.setStatus(RoomTypeStatus.INACTIVE);

		when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));

		mockMvc.perform(get("/api/room-types/1/price-preview")
						.param("checkIn", "2026-06-01")
						.param("checkOut", "2026-06-02"))
				.andExpect(status().isBadRequest());

		verify(roomTypeRepository).findById(1L);
		verify(pricingCalculator, never()).calculateBreakdown(any(), any(), any());
	}
}