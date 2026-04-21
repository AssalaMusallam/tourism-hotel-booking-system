package com.swer313.projectstep1.security;

import com.swer313.projectstep1.booking.Booking;
import com.swer313.projectstep1.booking.BookingRepository;
import com.swer313.projectstep1.payment.Payment;
import com.swer313.projectstep1.payment.PaymentRepository;
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
class PaymentSecurityServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private PaymentSecurityService service;

    @Test
    void adminAccess_allowed() {
        User u = new User(); u.setRole(UserRole.ADMIN);
        when(currentUserService.getCurrentUser()).thenReturn(u);

        assertTrue(service.canAccessPayment(1L, null));
    }

    @Test
    void guestAccess_matchesEmail_true() {
        User u = new User(); u.setRole(UserRole.GUEST); u.setEmail("g@e.com");
        when(currentUserService.getCurrentUser()).thenReturn(u);

        Payment p = new Payment(); p.setBookingId(10L);
        when(paymentRepository.findById(2L)).thenReturn(Optional.of(p));

        Booking b = new Booking(); b.setGuestEmail("g@e.com");
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(b));

        assertTrue(service.canAccessPayment(2L, null));
    }

    @Test
    void managerAccess_checksManagedHotel() {
        User u = new User(); u.setRole(UserRole.MANAGER);
        when(currentUserService.getCurrentUser()).thenReturn(u);

        Payment p = new Payment(); p.setBookingId(20L);
        when(paymentRepository.findById(3L)).thenReturn(Optional.of(p));

        Booking b = new Booking();
        var rt = new com.swer313.projectstep1.catalog.room.RoomType();
        var hotel = new com.swer313.projectstep1.catalog.hotel.Hotel(); hotel.setId(5L);
        rt.setHotel(hotel);
        b.setRoomType(rt);
        when(bookingRepository.findById(20L)).thenReturn(Optional.of(b));

        // manager has no managed hotels by default -> false
        assertFalse(service.canAccessPayment(3L, null));
    }
}


