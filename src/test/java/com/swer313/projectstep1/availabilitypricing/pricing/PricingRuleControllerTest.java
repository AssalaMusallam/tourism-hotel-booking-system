package com.swer313.projectstep1.availabilitypricing.pricing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swer313.projectstep1.availabilitypricing.availability.PagedResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PricingRuleController.class)
@AutoConfigureMockMvc(addFilters = false) // نعطل فلاتر الأمن حتى نختبر الكنترولر فقط
class PricingRuleControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private PricingRuleService pricingRuleService;

	// نترك هذا فقط حتى يفتح الـ ApplicationContext إذا احتاجه Spring من security config
	@MockBean
	private com.swer313.projectstep1.security.JwtService jwtService;

	private static final LocalDate START = LocalDate.of(2026, 5, 1);
	private static final LocalDate END = LocalDate.of(2026, 5, 10);

	/**
	 * Request صالح نستخدمه في أكثر من test.
	 */
	private PricingRuleRequest validRequest() {
		PricingRuleRequest req = new PricingRuleRequest();
		req.setName("Summer Peak");
		req.setDescription("Peak pricing");
		req.setStartDate(START);
		req.setEndDate(END);
		req.setPriceMultiplier(new BigDecimal("1.50"));
		return req;
	}

	/**
	 * Response جاهز نستخدمه في اختبارات الإنشاء والقراءة.
	 */
	private PricingRuleResponse sampleResponse(Long id) {
		return new PricingRuleResponse(
				id,
				"Summer Peak",
				"Peak pricing",
				START,
				END,
				new BigDecimal("1.50"),
				true,
				LocalDateTime.of(2026, 4, 1, 10, 0),
				LocalDateTime.of(2026, 4, 1, 10, 0)
		);
	}

	/**
	 * نبني PagedResponse بطريقة واضحة وآمنة.
	 */
	private PagedResponse<PricingRuleResponse> singleItemPage(PricingRuleResponse item) {
		Page<PricingRuleResponse> page =
				new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);

		return PagedResponse.from(page, List.of(item));
	}

	@Test
	void create_validBody_returns201AndCreatedRule() throws Exception {
		// Arrange
		PricingRuleRequest request = validRequest();
		PricingRuleResponse response = sampleResponse(1L);

		when(pricingRuleService.createRule(any(PricingRuleRequest.class))).thenReturn(response);

		// Act + Assert
		mockMvc.perform(post("/pricing-rules")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Summer Peak"))
				.andExpect(jsonPath("$.description").value("Peak pricing"))
				.andExpect(jsonPath("$.priceMultiplier").value(1.50));

		verify(pricingRuleService).createRule(any(PricingRuleRequest.class));
	}

	@Test
	void create_blankName_returns400() throws Exception {
		// Arrange
		PricingRuleRequest request = validRequest();
		request.setName(""); // يخالف @NotBlank

		// Act + Assert
		mockMvc.perform(post("/pricing-rules")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());

		verify(pricingRuleService, never()).createRule(any());
	}

	@Test
	void create_missingPriceMultiplier_returns400() throws Exception {
		// Arrange
		PricingRuleRequest request = validRequest();
		request.setPriceMultiplier(null); // يخالف @NotNull

		// Act + Assert
		mockMvc.perform(post("/pricing-rules")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());

		verify(pricingRuleService, never()).createRule(any());
	}

	@Test
	void create_multiplierTooHigh_returns400() throws Exception {
		// Arrange
		PricingRuleRequest request = validRequest();
		request.setPriceMultiplier(new BigDecimal("11.00")); // يخالف @DecimalMax

		// Act + Assert
		mockMvc.perform(post("/pricing-rules")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());

		verify(pricingRuleService, never()).createRule(any());
	}

	@Test
	void update_validBody_returns200AndUpdatedRule() throws Exception {
		// Arrange
		PricingRuleUpdateRequest update = new PricingRuleUpdateRequest();
		update.setName("Updated Peak");

		PricingRuleResponse response = new PricingRuleResponse(
				1L,
				"Updated Peak",
				"Peak pricing",
				START,
				END,
				new BigDecimal("1.50"),
				true,
				LocalDateTime.of(2026, 4, 1, 10, 0),
				LocalDateTime.of(2026, 4, 2, 11, 0)
		);

		when(pricingRuleService.updateRule(eq(1L), any(PricingRuleUpdateRequest.class)))
				.thenReturn(response);

		// Act + Assert
		mockMvc.perform(put("/pricing-rules/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(update)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Updated Peak"));

		verify(pricingRuleService).updateRule(eq(1L), any(PricingRuleUpdateRequest.class));
	}

	@Test
	void update_multiplierBelowMin_returns400() throws Exception {
		// Arrange
		PricingRuleUpdateRequest update = new PricingRuleUpdateRequest();
		update.setPriceMultiplier(new BigDecimal("0.05")); // أقل من @DecimalMin

		// Act + Assert
		mockMvc.perform(put("/pricing-rules/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(update)))
				.andExpect(status().isBadRequest());

		verify(pricingRuleService, never()).updateRule(any(), any());
	}

	@Test
	void delete_existingId_returns204() throws Exception {
		// Arrange
		doNothing().when(pricingRuleService).deleteRule(1L);

		// Act + Assert
		mockMvc.perform(delete("/pricing-rules/1"))
				.andExpect(status().isNoContent());

		verify(pricingRuleService).deleteRule(1L);
	}

	@Test
	void getById_existingId_returns200AndRule() throws Exception {
		// Arrange
		when(pricingRuleService.getById(1L)).thenReturn(sampleResponse(1L));

		// Act + Assert
		mockMvc.perform(get("/pricing-rules/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Summer Peak"))
				.andExpect(jsonPath("$.description").value("Peak pricing"));

		verify(pricingRuleService).getById(1L);
	}

	@Test
	void getAll_returns200WithPagedResponse() throws Exception {
		// Arrange
		when(pricingRuleService.getAllRules(any()))
				.thenReturn(singleItemPage(sampleResponse(1L)));

		// Act + Assert
		mockMvc.perform(get("/pricing-rules"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(1))
				.andExpect(jsonPath("$.content[0].name").value("Summer Peak"))
				.andExpect(jsonPath("$.pageNumber").value(0))
				.andExpect(jsonPath("$.pageSize").value(10))
				.andExpect(jsonPath("$.totalElements").value(1));

		verify(pricingRuleService).getAllRules(any());
	}

	@Test
	void getActive_returns200WithPagedResponse() throws Exception {
		// Arrange
		when(pricingRuleService.getActiveRules(any()))
				.thenReturn(singleItemPage(sampleResponse(2L)));

		// Act + Assert
		mockMvc.perform(get("/pricing-rules/active"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(2))
				.andExpect(jsonPath("$.content[0].name").value("Summer Peak"));

		verify(pricingRuleService).getActiveRules(any());
	}

	@Test
	void preview_validParams_returns200AndBreakdown() throws Exception {
		// Arrange
		PriceBreakdownDTO breakdown = new PriceBreakdownDTO(
				new BigDecimal("100.00"),
				2,
				List.of(),
				new BigDecimal("200.00"),
				new BigDecimal("0.16"),
				new BigDecimal("32.00"),
				new BigDecimal("232.00")
		);

		when(pricingRuleService.previewPrice(
				new BigDecimal("100.00"),
				START,
				END
		)).thenReturn(breakdown);

		// Act + Assert
		mockMvc.perform(get("/pricing-rules/preview")
						.param("basePrice", "100.00")
						.param("checkIn", START.toString())
						.param("checkOut", END.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.basePrice").value(100.00))
				.andExpect(jsonPath("$.subtotal").value(200.00))
				.andExpect(jsonPath("$.taxAmount").value(32.00))
				.andExpect(jsonPath("$.totalPrice").value(232.00));

		verify(pricingRuleService).previewPrice(new BigDecimal("100.00"), START, END);
	}
}