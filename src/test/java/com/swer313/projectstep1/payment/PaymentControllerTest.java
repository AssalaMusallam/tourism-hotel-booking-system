package com.swer313.projectstep1.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swer313.projectstep1.security.JwtAuthFilter;
import com.swer313.projectstep1.security.JwtService;
import com.swer313.projectstep1.security.PaymentSecurityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PaymentControllerTest.MethodSecurityTestConfig.class)
class PaymentControllerTest {

	@TestConfiguration
	@EnableMethodSecurity
	static class MethodSecurityTestConfig {
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private PaymentService paymentService;

	@MockBean(name = "paymentSecurityService")
	private PaymentSecurityService paymentSecurityService;

	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	@MockBean
	private JwtService jwtService;

	@MockBean
	private UserDetailsService userDetailsService;

	@Test
	@WithMockUser(roles = "ADMIN")
	void createIntent_success_returnsCreated() throws Exception {
		PaymentRequestDTO req = new PaymentRequestDTO();
		req.setBookingId(1L);
		req.setAmount(new BigDecimal("250.00"));
		req.setCurrency("USD");
		req.setMethod(PaymentMethod.MOCK_CARD);

		PaymentResponseDTO resp = new PaymentResponseDTO(
				10L, 1L, new BigDecimal("250.00"), "USD",
				PaymentMethod.MOCK_CARD, PaymentStatus.PENDING,
				"MOCK_GATEWAY", "pay_testref", null, null, null, null, null, null
		);

		when(paymentService.createIntent(any(PaymentRequestDTO.class))).thenReturn(resp);

		mockMvc.perform(post("/api/payments/intents")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.bookingId").value(1))
				.andExpect(jsonPath("$.transactionReference").value("pay_testref"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void createIntent_validationError_returnsBadRequest() throws Exception {
		String body = """
                {
                  "bookingId": 1
                }
                """;

		mockMvc.perform(post("/api/payments/intents")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	@WithMockUser(username = "guest")
	void getById_denied_returnsServerError_inCurrentGlobalHandlerSetup() throws Exception {
		when(paymentSecurityService.canAccessPayment(eq(5L), any())).thenReturn(false);

		mockMvc.perform(get("/api/payments/5"))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithMockUser(username = "guest")
	void getById_success_whenAuthorized() throws Exception {
		when(paymentSecurityService.canAccessPayment(eq(5L), any())).thenReturn(true);

		PaymentResponseDTO resp = new PaymentResponseDTO(
				5L, 2L, new BigDecimal("100.00"), "USD",
				PaymentMethod.MOCK_CARD, PaymentStatus.SUCCESS,
				"MOCK_GATEWAY", "pay_xxx", null, null, null, null, null, null
		);

		when(paymentService.getById(5L)).thenReturn(resp);

		mockMvc.perform(get("/api/payments/5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5))
				.andExpect(jsonPath("$.status").value("SUCCESS"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void getAll_invalidSort_returnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/payments").param("sort", "nope,asc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("Invalid sort field")));
	}
}