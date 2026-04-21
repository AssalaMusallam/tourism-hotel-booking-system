package com.swer313.projectstep1.security;

import com.swer313.projectstep1.booking.Booking;
import com.swer313.projectstep1.booking.BookingRepository;
import com.swer313.projectstep1.user.User;
import com.swer313.projectstep1.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingSecurityServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private BookingSecurityService service;

    @Test
    void canAccessBooking_guest_matchesEmail() {
        User u = new User(); u.setRole(UserRole.GUEST); u.setEmail("a@b.com");
        when(currentUserService.getCurrentUser()).thenReturn(u);

        Booking b = new Booking(); b.setGuestEmail("a@b.com");
        when(bookingRepository.findById(9L)).thenReturn(Optional.of(b));

        assertTrue(service.canAccessBooking(9L, null));
    }

    @Test
    void canAccessHotelBookings_manager_checksManage() {
        User u = new User(); u.setRole(UserRole.MANAGER);
        when(currentUserService.getCurrentUser()).thenReturn(u);

        assertFalse(service.canAccessHotelBookings(10L, null));
    }
}


