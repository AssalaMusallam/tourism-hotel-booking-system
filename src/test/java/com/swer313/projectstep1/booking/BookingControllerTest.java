package com.swer313.projectstep1.booking;

import com.swer313.projectstep1.catalog.room.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bookingController = new BookingController(bookingService);
    }

    @Test
    @DisplayName("create_guest_email_from_principal_used_and_returns_201")
    void create_guest_email_from_principal_used_and_returns_201() {
        BookingRequestDTO req = new BookingRequestDTO();
        req.setRoomTypeId(1L);
        req.setGuestName("Valid Guest");
        req.setGuestEmail("attacker@example.com");
        req.setGuestPhone("0599000001");
        req.setAdults(1);
        req.setChildren(0);
        req.setCheckIn(LocalDate.now().plusDays(5));
        req.setCheckOut(LocalDate.now().plusDays(6));
        req.setGuestNotes("test");

        UserDetails currentUser = User.withUsername("me@principal.com")
                .password("x")
                .roles("GUEST")
                .build();

        BookingResponseDTO resp = new BookingResponseDTO(
                101L, 1L, "Standard", "Hotel A",
                "Valid Guest", "me@principal.com", "0599000001",
                1, 0, 1,
                req.getCheckIn(), req.getCheckOut(), 1L,
                null, null, BookingStatus.PENDING,
                null, null, null, "test", null, null, 0L
        );

        when(bookingService.createBooking(any(BookingRequestDTO.class))).thenReturn(resp);

        ResponseEntity<BookingResponseDTO> response = bookingController.create(req, currentUser);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(101L, response.getBody().getId());
        assertEquals("me@principal.com", response.getBody().getGuestEmail());
        verify(bookingService).createBooking(any(BookingRequestDTO.class));
    }

    @Test
    @DisplayName("getMyBookings_uses_principal_and_returns_200")
    void getMyBookings_uses_principal_and_returns_200() {
        UserDetails currentUser = User.withUsername("me@a.com")
                .password("x")
                .roles("GUEST")
                .build();

        BookingResponseDTO booking = new BookingResponseDTO(
                201L, 2L, "Room", "Hotel",
                "Guest Name", "me@a.com", "0599000002",
                1, 0, 1,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2),
                1L, null, null, BookingStatus.PENDING,
                null, null, null, null, null, null, 0L
        );

        PagedResponse<BookingResponseDTO> page = new PagedResponse<>(
                List.of(booking), 0, 10, 1, 1, true, true, true, false
        );

        when(bookingService.getByGuestEmail(eq("me@a.com"), isNull(), any(PageRequest.class)))
                .thenReturn(page);

        ResponseEntity<PagedResponse<BookingResponseDTO>> response =
                bookingController.getMyBookings(currentUser, null, PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("me@a.com", response.getBody().getContent().get(0).getGuestEmail());
        verify(bookingService).getByGuestEmail(eq("me@a.com"), isNull(), any(PageRequest.class));
    }
}