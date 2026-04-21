package com.swer313.projectstep1.security;

import com.swer313.projectstep1.booking.Booking;
import com.swer313.projectstep1.booking.BookingRepository;
import com.swer313.projectstep1.user.User;
import com.swer313.projectstep1.user.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("bookingSecurityService")
public class BookingSecurityService {

    private final BookingRepository bookingRepository;
    private final CurrentUserService currentUserService;

    public BookingSecurityService(BookingRepository bookingRepository,
                                  CurrentUserService currentUserService) {
        this.bookingRepository = bookingRepository;
        this.currentUserService = currentUserService;
    }

    // ── canAccessHotelBookings ────────────────────────────────────
    public boolean canAccessHotelBookings(Long hotelId, Authentication auth) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser == null) return false; // ✅ أضفتها هون

        if (currentUser.getRole() == UserRole.ADMIN) return true;

        if (currentUser.getRole() == UserRole.MANAGER) {
            return currentUser.managesHotel(hotelId);
        }

        return false;
    }

    public boolean canAccessBooking(Long bookingId, Authentication auth) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser == null) return false; // ✅

        if (currentUser.getRole() == UserRole.ADMIN) return true;

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return false;

        if (currentUser.getRole() == UserRole.GUEST) {
            return booking.getGuestEmail() != null
                    && booking.getGuestEmail().equalsIgnoreCase(currentUser.getEmail());
        }

        if (currentUser.getRole() == UserRole.MANAGER) {
            Long bookingHotelId = booking.getRoomType().getHotel().getId();
            return currentUser.managesHotel(bookingHotelId);
        }

        return false;
    }
}