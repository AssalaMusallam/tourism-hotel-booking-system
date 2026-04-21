package com.swer313.projectstep1.booking;
import com.swer313.projectstep1.catalog.room.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    // ── Write ─────────────────────────────────────────────────────────────────

    /** Create a booking (PENDING). */
    BookingResponseDTO createBooking(BookingRequestDTO dto);

    /** PENDING → CONFIRMED. */
    BookingResponseDTO confirmBooking(Long bookingId);

    /** PENDING/CONFIRMED → CANCELLED (with 12h policy + refund). */
    BookingResponseDTO cancelBooking(Long bookingId, CancelBookingRequest request);

    /** CONFIRMED → COMPLETED. */
    BookingResponseDTO completeBooking(Long bookingId);

    // ── Read ──────────────────────────────────────────────────────────────────

    /** Get single booking by ID. */
    BookingResponseDTO getById(Long bookingId);

    /** Guest: get all my bookings by email. */
    PagedResponse<BookingResponseDTO> getByGuestEmail(
            String email, BookingStatus status, Pageable pageable);

    /** Manager: get all bookings for a hotel. */
    PagedResponse<BookingResponseDTO> getByHotel(
            Long hotelId, BookingStatus status, Pageable pageable);

    /** Admin/Manager: flexible search with all filters. */
    PagedResponse<BookingResponseDTO> getBookings(
            Long roomTypeId,
            Long hotelId,
            String guestEmail,
            BookingStatus status,
            LocalDate checkInFrom,  LocalDate checkInTo,
            LocalDate checkOutFrom, LocalDate checkOutTo,
            Pageable pageable);

    /** Manager: upcoming CONFIRMED bookings (checkIn >= today). */
    PagedResponse<BookingResponseDTO> getUpcomingBookings(Pageable pageable);
    /** Manager: upcoming CONFIRMED bookings for a specific hotel. */
    PagedResponse<BookingResponseDTO> getUpcomingByHotel(Long hotelId, Pageable pageable);



}