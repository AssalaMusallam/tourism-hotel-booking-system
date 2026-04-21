package com.swer313.projectstep1.booking;

import com.swer313.projectstep1.catalog.room.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController

@RequestMapping("/api/bookings")

@Tag(name = "6. Bookings", description = "Create and manage bookings")

public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // ── GUEST ─────────────────────────────────────────────────────────────────

    /**
     * إنشاء حجز — الـ guestEmail يُؤخذ من الـ JWT تلقائياً
     * المستخدم ما يقدر يحجز باسم شخص ثاني
     */
    @Operation(summary = "Create booking")
    @ApiResponse(responseCode = "201", description = "Booking created successfully")
    @PostMapping
    public ResponseEntity<BookingResponseDTO> create(
            @Valid @RequestBody BookingRequestDTO dto,
            @AuthenticationPrincipal UserDetails currentUser) {

        // نتجاهل الـ guestEmail اللي أرسله المستخدم ونستبدله بإيميله من الـ JWT
        dto.setGuestEmail(currentUser.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(dto));
    }

    @Operation(summary = "Get booking by ID")
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.canAccessBooking(#id, authentication)")
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    /**
     * حجوزاتي — الإيميل من الـ JWT مباشرة
     * ما في query param ?email= بعد الآن
     */
    @Operation(summary = "Get my bookings")
    @GetMapping("/my")
    public ResponseEntity<PagedResponse<BookingResponseDTO>> getMyBookings(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 10, sort = "checkIn") Pageable pageable) {

        return ResponseEntity.ok(
                bookingService.getByGuestEmail(
                        currentUser.getUsername(), status, pageable));
    }

    @Operation(summary = "Cancel booking")
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.canAccessBooking(#id, authentication)")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponseDTO> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelBookingRequest request) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, request));
    }

    // ── MANAGER ───────────────────────────────────────────────────────────────

    @Operation(summary = "Confirm booking")
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.canAccessBooking(#id, authentication)")
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<BookingResponseDTO> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }

    @Operation(summary = "Complete booking")
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.canAccessBooking(#id, authentication)")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<BookingResponseDTO> complete(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.completeBooking(id));
    }

    @Operation(summary = "Get bookings by hotel")
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.canAccessHotelBookings(#hotelId, authentication)")
    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<PagedResponse<BookingResponseDTO>> getByHotel(
            @PathVariable Long hotelId,
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 20, sort = "checkIn") Pageable pageable) {
        return ResponseEntity.ok(
                bookingService.getByHotel(hotelId, status, pageable));
    }

    @Operation(summary = "Get upcoming bookings")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/upcoming")
    public ResponseEntity<PagedResponse<BookingResponseDTO>> getUpcoming(
            @PageableDefault(size = 20, sort = "checkIn") Pageable pageable) {
        return ResponseEntity.ok(bookingService.getUpcomingBookings(pageable));
    }

    @Operation(summary = "Get upcoming bookings by hotel")
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.canAccessHotelBookings(#hotelId, authentication)")
    @GetMapping("/hotels/{hotelId}/upcoming")
    public ResponseEntity<PagedResponse<BookingResponseDTO>> getUpcomingByHotel(
            @PathVariable Long hotelId,
            @PageableDefault(size = 20, sort = "checkIn") Pageable pageable) {
        return ResponseEntity.ok(bookingService.getUpcomingByHotel(hotelId, pageable));
    }

    // ── ADMIN ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Get all bookings with filters")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PagedResponse<BookingResponseDTO>> getBookings(
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) String guestEmail,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInTo,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutTo,
            @PageableDefault(size = 20, sort = "checkIn") Pageable pageable) {

        return ResponseEntity.ok(bookingService.getBookings(
                roomTypeId, hotelId, guestEmail, status,
                checkInFrom, checkInTo,
                checkOutFrom, checkOutTo,
                pageable));
    }
}