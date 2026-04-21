package com.swer313.projectstep1.security;

import com.swer313.projectstep1.booking.Booking;
import com.swer313.projectstep1.booking.BookingRepository;
import com.swer313.projectstep1.payment.Payment;
import com.swer313.projectstep1.payment.PaymentRepository;
import com.swer313.projectstep1.user.User;
import com.swer313.projectstep1.user.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("paymentSecurityService")
public class PaymentSecurityService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final CurrentUserService currentUserService;

    public PaymentSecurityService(PaymentRepository paymentRepository,
                                  BookingRepository bookingRepository,
                                  CurrentUserService currentUserService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.currentUserService = currentUserService;
    }

    // ── canAccessPayment ──────────────────────────────────────────
    public boolean canAccessPayment(Long paymentId, Authentication auth) {
        User currentUser = currentUserService.getCurrentUser();

        if (currentUser.getRole() == UserRole.ADMIN) return true;

        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) return false;

        Booking booking = bookingRepository
                .findById(payment.getBookingId()).orElse(null);
        if (booking == null) return false;

        if (currentUser.getRole() == UserRole.GUEST) {
            return booking.getGuestEmail()
                    .equalsIgnoreCase(currentUser.getEmail());
        }

        if (currentUser.getRole() == UserRole.MANAGER) {
            Long hotelId = booking.getRoomType().getHotel().getId();
            return currentUser.managesHotel(hotelId); // ← بدل return true
        }

        return false;
    }

    // ── canAccessBookingPayment ───────────────────────────────────
    public boolean canAccessBookingPayment(Long bookingId, Authentication auth) {
        User currentUser = currentUserService.getCurrentUser();

        if (currentUser.getRole() == UserRole.ADMIN) return true;

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return false;

        if (currentUser.getRole() == UserRole.GUEST) {
            return booking.getGuestEmail()
                    .equalsIgnoreCase(currentUser.getEmail());
        }

        if (currentUser.getRole() == UserRole.MANAGER) {
            Long hotelId = booking.getRoomType().getHotel().getId();
            return currentUser.managesHotel(hotelId); // ← بدل return true
        }

        return false;
    }
    public boolean canAccessPaymentByReference(String ref, Authentication auth) {
        User currentUser = currentUserService.getCurrentUser();

        if (currentUser.getRole() == UserRole.ADMIN) return true;

        Payment payment = paymentRepository.findByTransactionReference(ref).orElse(null);
        if (payment == null) return false;

        Booking booking = bookingRepository.findById(payment.getBookingId()).orElse(null);
        if (booking == null) return false;

        if (currentUser.getRole() == UserRole.GUEST) {
            return booking.getGuestEmail().equalsIgnoreCase(currentUser.getEmail());
        }

        if (currentUser.getRole() == UserRole.MANAGER) {
            Long hotelId = booking.getRoomType().getHotel().getId();
            return currentUser.managesHotel(hotelId);
        }

        return false;
    }
}