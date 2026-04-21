package com.swer313.projectstep1.booking;

import com.swer313.projectstep1.availabilitypricing.pricing.PriceBreakdownDTO;
import com.swer313.projectstep1.availabilitypricing.pricing.PricingCalculator;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeNotFoundException;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import com.swer313.projectstep1.catalog.room.RoomTypeStatus;
import com.swer313.projectstep1.notification.NotificationDTOs;
import com.swer313.projectstep1.notification.NotificationService;
import com.swer313.projectstep1.waitinglist.WaitingListService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AvailabilityChecker availabilityChecker;

    @Mock
    private PricingCalculator pricingCalculator;

    @Mock
    private WaitingListService waitingListService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Captor
    private org.mockito.ArgumentCaptor<BookingRequestDTO> requestCaptor;

    private RoomType buildRoomType(Long id, String name, BigDecimal basePrice, int maxAdults, int maxChildren) {
        RoomType rt = new RoomType();
        ReflectionTestUtils.setField(rt, "id", id);
        rt.setName(name);
        rt.setBasePrice(basePrice);
        rt.setMaxAdults(maxAdults);
        rt.setMaxChildren(maxChildren);
        rt.setTotalUnits(5);

        com.swer313.projectstep1.catalog.hotel.Hotel hotel =
                new com.swer313.projectstep1.catalog.hotel.Hotel();
        ReflectionTestUtils.setField(hotel, "id", 99L);
        hotel.setName("H-Test");

        rt.setHotel(hotel);
        rt.setStatus(RoomTypeStatus.ACTIVE);
        return rt;
    }

    private BookingRequestDTO baseRequest(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        BookingRequestDTO dto = new BookingRequestDTO();
        dto.setRoomTypeId(roomTypeId);
        dto.setGuestName("John Doe");
        dto.setGuestEmail("john@example.com");
        dto.setGuestPhone("0599000001");
        dto.setAdults(2);
        dto.setChildren(0);
        dto.setCheckIn(checkIn);
        dto.setCheckOut(checkOut);
        dto.setGuestNotes("no notes");
        return dto;
    }

    @Test
    @DisplayName("createBooking_valid_input_creates_and_returns_dto")
    void createBooking_valid_input_creates_and_returns_dto() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = checkIn.plusDays(2);
        BookingRequestDTO dto = baseRequest(1L, checkIn, checkOut);

        RoomType rt = buildRoomType(1L, "Standard", new BigDecimal("100.00"), 2, 1);
        when(roomTypeRepository.findByIdWithLock(1L)).thenReturn(Optional.of(rt));
        when(availabilityChecker.isAvailable(1L, checkIn, checkOut)).thenReturn(true);

        PriceBreakdownDTO breakdown = new PriceBreakdownDTO(
                rt.getBasePrice(), 2, List.of(), new BigDecimal("200.00"),
                new BigDecimal("0.16"), new BigDecimal("32.00"), new BigDecimal("232.00")
        );
        when(pricingCalculator.calculateBreakdown(rt.getBasePrice(), checkIn, checkOut))
                .thenReturn(breakdown);

        Booking created = new Booking();
        ReflectionTestUtils.setField(created, "id", 5L);
        created.setRoomType(rt);
        created.setGuestEmail(dto.getGuestEmail());
        created.setGuestName(dto.getGuestName());
        created.setCheckIn(checkIn);
        created.setCheckOut(checkOut);
        created.setTotalPrice(breakdown.getTotalPrice());

        when(bookingMapper.toEntity(any(), eq(rt), any(), any())).thenReturn(created);
        when(bookingRepository.save(created)).thenReturn(created);
        when(availabilityChecker.remainingUnits(1L, checkIn, checkOut)).thenReturn(3L);

        BookingResponseDTO dtoOut = new BookingResponseDTO(
                5L, 1L, rt.getName(), rt.getHotel().getName(),
                dto.getGuestName(), dto.getGuestEmail(), dto.getGuestPhone(),
                dto.getAdults(), dto.getChildren(), dto.getAdults() + dto.getChildren(),
                dto.getCheckIn(), dto.getCheckOut(), 2L,
                rt.getBasePrice(), breakdown.getTotalPrice(),
                BookingStatus.PENDING, null, null, null,
                dto.getGuestNotes(), null, null, 3L
        );
        when(bookingMapper.toDto(created, 3L)).thenReturn(dtoOut);

        BookingResponseDTO res = bookingService.createBooking(dto);

        assertNotNull(res);
        assertEquals(5L, res.getId());
        assertEquals(3L, res.getRemainingUnits());
        verify(roomTypeRepository).findByIdWithLock(1L);
        verify(pricingCalculator).calculateBreakdown(rt.getBasePrice(), checkIn, checkOut);
        verify(bookingRepository).save(created);
        verify(notificationService).send(any(NotificationDTOs.SendRequest.class));
    }

    @Test
    @DisplayName("createBooking_invalid_date_range_throws_InvalidDateRangeException")
    void createBooking_invalid_date_range_throws_InvalidDateRangeException() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = checkIn;
        BookingRequestDTO dto = baseRequest(1L, checkIn, checkOut);

        assertThrows(InvalidDateRangeException.class, () -> bookingService.createBooking(dto));
        verifyNoInteractions(roomTypeRepository, bookingRepository);
    }

    @Test
    @DisplayName("createBooking_roomtype_missing_throws_RoomTypeNotFoundException")
    void createBooking_roomtype_missing_throws_RoomTypeNotFoundException() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = checkIn.plusDays(1);
        BookingRequestDTO dto = baseRequest(99L, checkIn, checkOut);

        when(roomTypeRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        assertThrows(RoomTypeNotFoundException.class, () -> bookingService.createBooking(dto));
        verify(roomTypeRepository).findByIdWithLock(99L);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    @DisplayName("createBooking_inactive_room_throws_RoomNotAvailableException")
    void createBooking_inactive_room_throws_RoomNotAvailableException() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = checkIn.plusDays(1);
        BookingRequestDTO dto = baseRequest(2L, checkIn, checkOut);

        RoomType rt = buildRoomType(2L, "Inactive", new BigDecimal("80.00"), 2, 0);
        rt.setStatus(RoomTypeStatus.INACTIVE);
        when(roomTypeRepository.findByIdWithLock(2L)).thenReturn(Optional.of(rt));

        assertThrows(RoomNotAvailableException.class, () -> bookingService.createBooking(dto));
        verify(roomTypeRepository).findByIdWithLock(2L);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    @DisplayName("createBooking_guest_capacity_exceeded_throws_GuestCapacityExceededException")
    void createBooking_guest_capacity_exceeded_throws_GuestCapacityExceededException() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = checkIn.plusDays(2);
        BookingRequestDTO dto = baseRequest(3L, checkIn, checkOut);
        dto.setAdults(4);
        dto.setChildren(2);

        RoomType rt = buildRoomType(3L, "Small", new BigDecimal("70.00"), 2, 1);
        when(roomTypeRepository.findByIdWithLock(3L)).thenReturn(Optional.of(rt));

        assertThrows(GuestCapacityExceededException.class, () -> bookingService.createBooking(dto));
        verify(roomTypeRepository).findByIdWithLock(3L);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    @DisplayName("createBooking_unavailable_by_checker_throws_RoomNotAvailableException")
    void createBooking_unavailable_by_checker_throws_RoomNotAvailableException() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = checkIn.plusDays(2);
        BookingRequestDTO dto = baseRequest(4L, checkIn, checkOut);

        RoomType rt = buildRoomType(4L, "Busy", new BigDecimal("120.00"), 2, 0);
        when(roomTypeRepository.findByIdWithLock(4L)).thenReturn(Optional.of(rt));
        when(availabilityChecker.isAvailable(4L, checkIn, checkOut)).thenReturn(false);

        assertThrows(RoomNotAvailableException.class, () -> bookingService.createBooking(dto));
        verify(roomTypeRepository).findByIdWithLock(4L);
        verify(availabilityChecker).isAvailable(4L, checkIn, checkOut);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    @DisplayName("createBooking_notification_failure_does_not_fail_creation")
    void createBooking_notification_failure_does_not_fail_creation() {
        LocalDate checkIn = LocalDate.now().plusDays(4);
        LocalDate checkOut = checkIn.plusDays(1);
        BookingRequestDTO dto = baseRequest(5L, checkIn, checkOut);

        RoomType rt = buildRoomType(5L, "OK", new BigDecimal("90.00"), 2, 1);
        when(roomTypeRepository.findByIdWithLock(5L)).thenReturn(Optional.of(rt));
        when(availabilityChecker.isAvailable(5L, checkIn, checkOut)).thenReturn(true);

        PriceBreakdownDTO breakdown = new PriceBreakdownDTO(
                rt.getBasePrice(), 1, List.of(), rt.getBasePrice(),
                new BigDecimal("0.16"), new BigDecimal("14.40"), new BigDecimal("104.40")
        );
        when(pricingCalculator.calculateBreakdown(rt.getBasePrice(), checkIn, checkOut))
                .thenReturn(breakdown);

        Booking b = new Booking();
        ReflectionTestUtils.setField(b, "id", 12L);
        b.setRoomType(rt);
        b.setGuestEmail(dto.getGuestEmail());
        b.setGuestName(dto.getGuestName());
        b.setCheckIn(checkIn);
        b.setCheckOut(checkOut);
        b.setTotalPrice(breakdown.getTotalPrice());

        when(bookingMapper.toEntity(any(), eq(rt), any(), any())).thenReturn(b);
        when(bookingRepository.save(b)).thenReturn(b);
        when(availabilityChecker.remainingUnits(5L, checkIn, checkOut)).thenReturn(4L);

        when(bookingMapper.toDto(b, 4L)).thenReturn(new BookingResponseDTO(
                12L, 5L, rt.getName(), rt.getHotel().getName(),
                dto.getGuestName(), dto.getGuestEmail(), dto.getGuestPhone(),
                dto.getAdults(), dto.getChildren(), dto.getAdults() + dto.getChildren(),
                dto.getCheckIn(), dto.getCheckOut(), 1L,
                rt.getBasePrice(), breakdown.getTotalPrice(),
                BookingStatus.PENDING, null, null, null,
                dto.getGuestNotes(), null, null, 4L
        ));

        doThrow(new RuntimeException("SMTP down")).when(notificationService).send(any(NotificationDTOs.SendRequest.class));

        BookingResponseDTO res = bookingService.createBooking(dto);

        assertNotNull(res);
        assertEquals(12L, res.getId());
        verify(notificationService).send(any(NotificationDTOs.SendRequest.class));
    }

    @Test
    @DisplayName("confirmBooking_pending_transitions_to_confirmed_and_sends_notification")
    void confirmBooking_pending_transitions_to_confirmed_and_sends_notification() {
        Booking b = new Booking();
        ReflectionTestUtils.setField(b, "id", 21L);
        b.setStatus(BookingStatus.PENDING);
        b.setCheckIn(LocalDate.now().plusDays(2));
        b.setCheckOut(LocalDate.now().plusDays(3));
        b.setGuestEmail("g@example.com");
        b.setGuestName("Guest");

        RoomType rt = buildRoomType(7L, "R7", new BigDecimal("50.00"), 2, 0);
        b.setRoomType(rt);
        b.setTotalPrice(new BigDecimal("60.00"));

        when(bookingRepository.findById(21L)).thenReturn(Optional.of(b));
        when(bookingRepository.save(b)).thenReturn(b);
        when(availabilityChecker.remainingUnits(rt.getId(), b.getCheckIn(), b.getCheckOut())).thenReturn(2L);
        when(bookingMapper.toDto(b, 2L)).thenReturn(new BookingResponseDTO(
                21L, rt.getId(), rt.getName(), rt.getHotel().getName(),
                "Guest", "g@example.com", "0599000001", 1, 0, 1,
                b.getCheckIn(), b.getCheckOut(), 1L,
                rt.getBasePrice(), new BigDecimal("60.00"),
                BookingStatus.CONFIRMED, null, null, null, null, null, null, 2L
        ));

        BookingResponseDTO res = bookingService.confirmBooking(21L);

        assertNotNull(res);
        assertEquals(BookingStatus.CONFIRMED, b.getStatus());
        verify(bookingRepository).save(b);
        verify(notificationService).sendBookingConfirmed(any(NotificationDTOs.BookingConfirmedEvent.class));
    }

    @Test
    @DisplayName("confirmBooking_not_found_throws_BookingNotFoundException")
    void confirmBooking_not_found_throws_BookingNotFoundException() {
        assertThrows(BookingNotFoundException.class, () -> bookingService.confirmBooking(999L));
    }

    @Test
    @DisplayName("confirmBooking_invalid_status_throws_InvalidBookingStatusTransitionException")
    void confirmBooking_invalid_status_throws_InvalidBookingStatusTransitionException() {
        Booking b = new Booking();
        ReflectionTestUtils.setField(b, "id", 30L);
        b.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(30L)).thenReturn(Optional.of(b));

        assertThrows(InvalidBookingStatusTransitionException.class, () -> bookingService.confirmBooking(30L));
    }

    @Test
    @DisplayName("completeBooking_confirmed_completes_successfully")
    void completeBooking_confirmed_completes_successfully() {
        Booking b = new Booking();
        ReflectionTestUtils.setField(b, "id", 40L);
        b.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(40L)).thenReturn(Optional.of(b));
        when(bookingRepository.save(b)).thenReturn(b);
        when(bookingMapper.toDto(b, 0L)).thenReturn(new BookingResponseDTO(
                40L, null, null, null,
                "G", "g@example.com", "0599000001", 1, 0, 1,
                LocalDate.now().minusDays(3), LocalDate.now().minusDays(1), 2L,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BookingStatus.COMPLETED, null, null, null, null, null, null, 0L
        ));

        BookingResponseDTO out = bookingService.completeBooking(40L);

        assertNotNull(out);
        assertEquals(BookingStatus.COMPLETED, b.getStatus());
        verify(bookingRepository).save(b);
    }

    @Test
    @DisplayName("completeBooking_not_found_throws_BookingNotFoundException")
    void completeBooking_not_found_throws_BookingNotFoundException() {
        when(bookingRepository.findById(1234L)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> bookingService.completeBooking(1234L));
    }

    @Test
    @DisplayName("completeBooking_invalid_status_throws_InvalidBookingStatusTransitionException")
    void completeBooking_invalid_status_throws_InvalidBookingStatusTransitionException() {
        Booking b = new Booking();
        ReflectionTestUtils.setField(b, "id", 41L);
        b.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(41L)).thenReturn(Optional.of(b));

        assertThrows(InvalidBookingStatusTransitionException.class, () -> bookingService.completeBooking(41L));
    }

    @Test
    @DisplayName("cancelBooking_allowed_full_refund_when_3_or_more_days_before_checkin")
    void cancelBooking_allowed_full_refund_when_3_or_more_days_before_checkin() {
        LocalDate checkIn = LocalDate.now().plusDays(4);
        LocalDate checkOut = checkIn.plusDays(2);

        Booking b = new Booking();
        ReflectionTestUtils.setField(b, "id", 50L);
        RoomType rt = buildRoomType(10L, "C", new BigDecimal("100.00"), 2, 0);
        b.setRoomType(rt);
        b.setGuestEmail("g@example.com");
        b.setGuestName("Guest");
        b.setCheckIn(checkIn);
        b.setCheckOut(checkOut);
        b.setTotalPrice(new BigDecimal("300.00"));
        b.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(50L)).thenReturn(Optional.of(b));
        when(bookingRepository.save(b)).thenReturn(b);
        when(availabilityChecker.remainingUnits(rt.getId(), checkIn, checkOut)).thenReturn(5L);
        when(bookingMapper.toDto(b, 5L)).thenReturn(new BookingResponseDTO(
                50L, rt.getId(), rt.getName(), rt.getHotel().getName(),
                "Guest", "g@example.com", "0599000001", 1, 0, 1,
                checkIn, checkOut, 2L, rt.getBasePrice(), b.getTotalPrice(), BookingStatus.CANCELLED,
                b.getCancelledAt(), "reason", b.getRefundAmount(), null, null, null, 5L
        ));

        CancelBookingRequest req = new CancelBookingRequest();
        req.setReason("Change plans");

        BookingResponseDTO out = bookingService.cancelBooking(50L, req);

        assertNotNull(out);
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
        assertNotNull(b.getCancelledAt());
        assertEquals("Change plans", b.getCancellationReason());
        assertNotNull(b.getRefundAmount());
        assertEquals(0, b.getRefundAmount().compareTo(new BigDecimal("300.00")));
        verify(waitingListService).notifyNextInQueue(rt.getId(), checkIn, checkOut);
        verify(notificationService).sendBookingCancelled(any(NotificationDTOs.BookingCancelledEvent.class));
    }

    @Test
    @DisplayName("cancelBooking_partial_refund_when_1_to_2_days_before_checkin")
    void cancelBooking_partial_refund_when_1_to_2_days_before_checkin() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(2);

        Booking b = new Booking();
        ReflectionTestUtils.setField(b, "id", 51L);
        RoomType rt = buildRoomType(11L, "P", new BigDecimal("75.00"), 2, 0);
        b.setRoomType(rt);
        b.setGuestEmail("g@example.com");
        b.setGuestName("Guest");
        b.setCheckIn(checkIn);
        b.setCheckOut(checkOut);
        b.setTotalPrice(new BigDecimal("200.00"));
        b.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(51L)).thenReturn(Optional.of(b));
        when(bookingRepository.save(b)).thenReturn(b);
        when(availabilityChecker.remainingUnits(rt.getId(), checkIn, checkOut)).thenReturn(5L);
        when(bookingMapper.toDto(b, 5L)).thenReturn(new BookingResponseDTO(
                51L, rt.getId(), rt.getName(), rt.getHotel().getName(),
                "Guest", "g@example.com", "0599000001", 1, 0, 1,
                checkIn, checkOut, 2L, rt.getBasePrice(), b.getTotalPrice(), BookingStatus.CANCELLED,
                b.getCancelledAt(), "reason", b.getRefundAmount(), null, null, null, 5L
        ));

        CancelBookingRequest req = new CancelBookingRequest();
        req.setReason("Emergency");

        BookingResponseDTO out = bookingService.cancelBooking(51L, req);

        assertNotNull(out);
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
        assertNotNull(b.getCancelledAt());
        assertEquals("Emergency", b.getCancellationReason());
        assertNotNull(b.getRefundAmount());
        assertEquals(0, b.getRefundAmount().compareTo(new BigDecimal("100.00")));
        verify(waitingListService).notifyNextInQueue(rt.getId(), checkIn, checkOut);
        verify(notificationService).sendBookingCancelled(any(NotificationDTOs.BookingCancelledEvent.class));
    }

    @Test
    @DisplayName("cancelBooking_within_12_hours_throws_CancellationNotAllowedException")
    void cancelBooking_within_12_hours_throws_CancellationNotAllowedException() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);

        Booking b = new Booking();
        ReflectionTestUtils.setField(b, "id", 52L);
        RoomType rt = buildRoomType(12L, "N", new BigDecimal("90.00"), 2, 0);
        b.setRoomType(rt);
        b.setGuestEmail("g@example.com");
        b.setGuestName("Guest");
        b.setCheckIn(checkIn);
        b.setCheckOut(checkOut);
        b.setTotalPrice(new BigDecimal("180.00"));
        b.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(52L)).thenReturn(Optional.of(b));

        CancelBookingRequest req = new CancelBookingRequest();
        req.setReason("Late cancellation");

        assertThrows(CancellationNotAllowedException.class, () -> bookingService.cancelBooking(52L, req));
    }

    @Test
    @DisplayName("cancelBooking_not_found_throws_BookingNotFoundException")
    void cancelBooking_not_found_throws_BookingNotFoundException() {
        when(bookingRepository.findById(500L)).thenReturn(Optional.empty());

        CancelBookingRequest req = new CancelBookingRequest();
        req.setReason("x");

        assertThrows(BookingNotFoundException.class, () -> bookingService.cancelBooking(500L, req));
    }

    @Test
    @DisplayName("cancelBooking_invalid_status_throws_CancellationNotAllowedException")
    void cancelBooking_invalid_status_throws_CancellationNotAllowedException() {
        Booking b = new Booking();
        ReflectionTestUtils.setField(b, "id", 53L);
        b.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(53L)).thenReturn(Optional.of(b));

        CancelBookingRequest req = new CancelBookingRequest();
        req.setReason("x");

        assertThrows(CancellationNotAllowedException.class, () -> bookingService.cancelBooking(53L, req));
    }
}