package com.swer313.projectstep1.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private NotificationService notificationService;

	@Test
	@WithMockUser(roles = "ADMIN")
	void send_bookingConfirmed_admin_allowed() throws Exception {
		NotificationDTOs.BookingConfirmedEvent ev = NotificationDTOs.BookingConfirmedEvent.builder()
				.bookingId(1L)
				.guestEmail("g@e.com")
				.guestName("G")
				.build();

		when(notificationService.sendBookingConfirmed(any())).thenReturn(NotificationDTOs.NotificationResponse.builder().id(1L).build());

		mockMvc.perform(post("/api/v1/notifications/events/booking-confirmed")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(ev)))
				.andExpect(status().isCreated());
	}
}