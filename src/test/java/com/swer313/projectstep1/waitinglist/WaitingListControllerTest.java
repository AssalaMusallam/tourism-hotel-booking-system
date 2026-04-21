package com.swer313.projectstep1.waitinglist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swer313.projectstep1.catalog.room.PagedResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WaitingListController.class)
class WaitingListControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private WaitingListService waitingListService;

	@MockBean
	private com.swer313.projectstep1.security.CurrentUserService currentUserService;

	@MockBean
	private com.swer313.projectstep1.security.JwtService jwtService;

	@Test
	@WithMockUser
	void join_returnsCreated() throws Exception {
		WaitingListRequestDTO req = new WaitingListRequestDTO();
		req.setRoomTypeId(1L);
		req.setGuestEmail("g@e.com");
		req.setGuestName("Guest");
		req.setCheckIn(LocalDate.now().plusDays(2));
		req.setCheckOut(LocalDate.now().plusDays(3));

		when(currentUserService.getCurrentUserEmail()).thenReturn("g@e.com");

		WaitingListResponseDTO resp = new WaitingListResponseDTO(
				10L,
				1L,
				"RT",
				"H",
				"g@e.com",
				"Guest",
				req.getCheckIn(),
				req.getCheckOut(),
				WaitingListStatus.WAITING,
				LocalDateTime.now(),
				null,
				1
		);

		when(waitingListService.joinWaitingList(any())).thenReturn(resp);

		mockMvc.perform(post("/api/waiting-list")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isCreated());
	}

	@Test
	@WithMockUser
	void cancel_returnsNoContent() throws Exception {
		when(currentUserService.getCurrentUserEmail()).thenReturn("g@e.com");
		doNothing().when(waitingListService).cancelEntry(5L, "g@e.com");

		mockMvc.perform(delete("/api/waiting-list/5")
						.with(csrf()))
				.andExpect(status().isNoContent());
	}

	@Test
	@WithMockUser
	void getMyEntries_returnsPaged() throws Exception {
		@SuppressWarnings("unchecked")
		PagedResponse<WaitingListResponseDTO> pagedResponse =
				(PagedResponse<WaitingListResponseDTO>) mock(PagedResponse.class);

		when(currentUserService.getCurrentUserEmail()).thenReturn("g@e.com");
		when(waitingListService.getMyEntries(anyString(), any()))
				.thenReturn(pagedResponse);

		mockMvc.perform(get("/api/waiting-list/my"))
				.andExpect(status().isOk());
	}
}