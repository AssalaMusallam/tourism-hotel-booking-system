package com.swer313.projectstep1.booking;
import com.swer313.projectstep1.availabilitypricing.pricing.PriceBreakdownDTO;
import com.swer313.projectstep1.availabilitypricing.pricing.PricingCalculator;
import com.swer313.projectstep1.catalog.room.PagedResponse;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeNotFoundException;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import com.swer313.projectstep1.notification.NotificationDTOs;
import com.swer313.projectstep1.notification.NotificationService;
import com.swer313.projectstep1.notification.NotificationType;
import com.swer313.projectstep1.notification.ReferenceType;
import com.swer313.projectstep1.waitinglist.WaitingListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    // ── Cancellation policy ───────────────────────────────────────────────────
    private static final int    CANCELLATION_WINDOW_HOURS = 12;
    private static final int    FULL_REFUND_DAYS          = 3;
    private static final int    PARTIAL_REFUND_DAYS       = 1;
    private static final double PARTIAL_REFUND_PERCENT    = 0.50;

    // ── Scheduler ─────────────────────────────────────────────────────────────
    private static final int REMINDER_DAYS_BEFORE = 2;

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final BookingRepository   bookingRepository;
    private final RoomTypeRepository  roomTypeRepository;
    private final BookingMapper       bookingMapper;
    private final NotificationService notificationService;
    private final AvailabilityChecker availabilityChecker;
    private final PricingCalculator   pricingCalculator;
    private final WaitingListService waitingListService;

    public BookingServiceImpl(
            BookingRepository   bookingRepository,
            RoomTypeRepository  roomTypeRepository,
            BookingMapper       bookingMapper,
            NotificationService notificationService,
            AvailabilityChecker availabilityChecker,
            PricingCalculator   pricingCalculator,
            WaitingListService  waitingListService) {
        this.bookingRepository   = bookingRepository;
        this.roomTypeRepository  = roomTypeRepository;
        this.bookingMapper       = bookingMapper;
        this.notificationService = notificationService;
        this.availabilityChecker = availabilityChecker;
        this.pricingCalculator   = pricingCalculator;
        this.waitingListService  = waitingListService;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CREATE
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public BookingResponseDTO createBooking(BookingRequestDTO dto) {

        // 1. Date range validation
        validateDates(dto.getCheckIn(), dto.getCheckOut());

        // 2. Pessimistic lock على الـ RoomType — يمنع race condition نهائياً
        //    SELECT ... FOR UPDATE: أي thread ثاني بيستنى حتى تنتهي الـ transaction
        RoomType roomType = roomTypeRepository.findByIdWithLock(dto.getRoomTypeId())
                .orElseThrow(() -> new RoomTypeNotFoundException(dto.getRoomTypeId()));

        // 3. الغرفة لازم تكون ACTIVE وعندها وحدات فيزيائية
        if (!roomType.isActiveAndAvailable())
            throw new RoomNotAvailableException(
                    dto.getRoomTypeId(), dto.getCheckIn(), dto.getCheckOut());

        // 4. Capacity check — adults + children <= maxCapacity
        int totalGuests = dto.getAdults() + dto.getChildren();
        if (totalGuests > roomType.getMaxCapacity())
            throw new GuestCapacityExceededException(
                    totalGuests, roomType.getMaxCapacity(), roomType.getName());

        // 5. Availability check بعد الـ lock — يحسب الحجوزات الحالية
        //    isAvailable() داخل AvailabilityCheckerAdapter هو أيضاً @Transactional
        //    ويستخدم نفس الـ lock — نضمن ما في race condition
        if (!availabilityChecker.isAvailable(
                dto.getRoomTypeId(), dto.getCheckIn(), dto.getCheckOut()))
            throw new RoomNotAvailableException(
                    dto.getRoomTypeId(), dto.getCheckIn(), dto.getCheckOut());

        // 6. Price calculation — seasonal rules + weekend multiplier + tax
        //    FIX: pricePerNight = basePrice من الـ RoomType (مو أول ليلة)
        //    لأن أول ليلة قد تكون weekend أو seasonal وما تمثل السعر الأساسي
        PriceBreakdownDTO breakdown = pricingCalculator.calculateBreakdown(
                roomType.getBasePrice(), dto.getCheckIn(), dto.getCheckOut());

        BigDecimal totalPrice    = breakdown.getTotalPrice();
        BigDecimal pricePerNight = roomType.getBasePrice(); // ← FIX: السعر الأساسي

        // 7. Build and persist
        Booking booking = bookingMapper.toEntity(dto, roomType, pricePerNight, totalPrice);
        Booking saved   = bookingRepository.save(booking);

        // 8. Remaining units بعد الحجز — لإظهاره في الـ response
        long remaining = availabilityChecker.remainingUnits(
                dto.getRoomTypeId(), dto.getCheckIn(), dto.getCheckOut());

        // 9. Notification — fire-and-forget، لو فشلت ما تأثر على الـ booking
        sendPendingNotificationSafely(saved);

        return bookingMapper.toDto(saved, remaining);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CONFIRM  PENDING → CONFIRMED
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public BookingResponseDTO confirmBooking(Long bookingId) {
        Booking booking = findOrThrow(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING)
            throw new InvalidBookingStatusTransitionException(
                    bookingId, booking.getStatus(), BookingStatus.CONFIRMED);

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);

        sendConfirmedNotificationSafely(saved);

        long remaining = availabilityChecker.remainingUnits(
                saved.getRoomType().getId(),
                saved.getCheckIn(),
                saved.getCheckOut());

        return bookingMapper.toDto(saved, remaining);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // COMPLETE  CONFIRMED → COMPLETED
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public BookingResponseDTO completeBooking(Long bookingId) {
        Booking booking = findOrThrow(bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED)
            throw new InvalidBookingStatusTransitionException(
                    bookingId, booking.getStatus(), BookingStatus.COMPLETED);

        booking.setStatus(BookingStatus.COMPLETED);
        Booking saved = bookingRepository.save(booking);

        return bookingMapper.toDto(saved, 0L);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CANCEL  PENDING/CONFIRMED → CANCELLED
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public BookingResponseDTO cancelBooking(Long bookingId, CancelBookingRequest request) {
        Booking booking = findOrThrow(bookingId);

        // 1. الحجز لازم يكون PENDING أو CONFIRMED
        if (!booking.isCancellable())
            throw new CancellationNotAllowedException(
                    "Booking " + bookingId + " cannot be cancelled — status is "
                            + booking.getStatus());

        // 2. سياسة الـ 12 ساعة — لازم يلغي قبل 12 ساعة من الـ check-in
        long hoursLeft = booking.hoursUntilCheckIn();
        if (hoursLeft < CANCELLATION_WINDOW_HOURS)
            throw new CancellationNotAllowedException(String.format(
                    "Cancellation not allowed within %d hours of check-in. Hours remaining: %d.",
                    CANCELLATION_WINDOW_HOURS, Math.max(hoursLeft, 0)));

        // 3. احسب الـ refund حسب عدد الأيام للـ check-in
        BigDecimal refund = calculateRefund(booking);

        // 4. حدّث الـ booking
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(request.getReason());
        booking.setRefundAmount(refund);
        Booking saved = bookingRepository.save(booking);

        sendCancelledNotificationSafely(saved);
        notifyWaitingListSafely(saved);

        // 5. Remaining بتزيد بعد الإلغاء
        long remaining = availabilityChecker.remainingUnits(
                saved.getRoomType().getId(),
                saved.getCheckIn(),
                saved.getCheckOut());

        return bookingMapper.toDto(saved, remaining);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // READ
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO getById(Long bookingId) {
        Booking b = findOrThrow(bookingId);

        // للـ CANCELLED/COMPLETED ما نحسب remaining — مش مفيدة ومكلفة
        long remaining = (b.getStatus() == BookingStatus.CANCELLED
                || b.getStatus() == BookingStatus.COMPLETED)
                ? 0L
                : availabilityChecker.remainingUnits(
                b.getRoomType().getId(), b.getCheckIn(), b.getCheckOut());

        return bookingMapper.toDto(b, remaining);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponseDTO> getByGuestEmail(
            String email, BookingStatus status, Pageable pageable) {

        Page<Booking> page = status != null
                ? bookingRepository.findByGuestEmailIgnoreCaseAndStatus(
                email.trim(), status, pageable)
                : bookingRepository.findByGuestEmailIgnoreCase(
                email.trim(), pageable);

        return toPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponseDTO> getByHotel(
            Long hotelId, BookingStatus status, Pageable pageable) {

        Page<Booking> page = status != null
                ? bookingRepository.findByRoomType_Hotel_IdAndStatus(
                hotelId, status, pageable)
                : bookingRepository.findByRoomType_Hotel_Id(
                hotelId, pageable);

        return toPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponseDTO> getBookings(
            Long roomTypeId, Long hotelId, String guestEmail,
            BookingStatus status,
            LocalDate checkInFrom,  LocalDate checkInTo,
            LocalDate checkOutFrom, LocalDate checkOutTo,
            Pageable pageable) {

        validateDateFilter("checkIn",  checkInFrom,  checkInTo);
        validateDateFilter("checkOut", checkOutFrom, checkOutTo);

        Specification<Booking> spec = BookingSpecification.withFilters(
                roomTypeId, hotelId, guestEmail, status,
                checkInFrom, checkInTo, checkOutFrom, checkOutTo);

        return toPagedResponse(bookingRepository.findAll(spec, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponseDTO> getUpcomingBookings(Pageable pageable) {
        return toPagedResponse(
                bookingRepository.findAll(
                        BookingSpecification.upcoming(), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponseDTO> getUpcomingByHotel(
            Long hotelId, Pageable pageable) {
        // FIX: استخدام overload مخصص بدل chain — أنظف وأوضح
        return toPagedResponse(
                bookingRepository.findAll(
                        BookingSpecification.upcomingByHotel(hotelId), pageable));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SCHEDULER — reminder يومي الساعة 9 صباحاً
    // ══════════════════════════════════════════════════════════════════════════

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendCheckInReminders() {
        LocalDate reminderDate = LocalDate.now().plusDays(REMINDER_DAYS_BEFORE);

        List<Booking> upcoming = bookingRepository
                .findConfirmedWithRoomTypeAndHotel(BookingStatus.CONFIRMED, reminderDate);

        log.info("Sending check-in reminders for {} bookings on {}",
                upcoming.size(), reminderDate);

        for (Booking b : upcoming) {
            try {
                notificationService.send(
                        NotificationDTOs.SendRequest.builder()
                                .recipientEmail(b.getGuestEmail())
                                .recipientName(b.getGuestName())
                                .type(NotificationType.BOOKING_REMINDER)
                                .referenceId(b.getId())
                                .referenceType(ReferenceType.BOOKING)
                                .hotelName(b.getRoomType().getHotel().getName())
                                .roomType(b.getRoomType().getName())
                                .checkInDate(b.getCheckIn().toString())
                                .checkOutDate(b.getCheckOut().toString())
                                .totalAmount(b.getTotalPrice().toPlainString())
                                .bookingReference("BK-" + b.getId())
                                .build());
            } catch (Exception e) {
                log.warn("Failed to send reminder for booking {}: {}",
                        b.getId(), e.getMessage());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private — Notification helpers
    // ══════════════════════════════════════════════════════════════════════════

    private void sendPendingNotificationSafely(Booking b) {
        try {
            notificationService.send(buildSendRequest(b, NotificationType.BOOKING_PENDING));
        } catch (Exception e) {
            log.warn("Failed to send PENDING notification for booking {}: {}",
                    b.getId(), e.getMessage());
        }
    }

    private void sendConfirmedNotificationSafely(Booking b) {
        try {
            notificationService.sendBookingConfirmed(
                    NotificationDTOs.BookingConfirmedEvent.builder()
                            .bookingId(b.getId())
                            .guestEmail(b.getGuestEmail())
                            .guestName(b.getGuestName())
                            .hotelName(b.getRoomType().getHotel().getName())
                            .roomType(b.getRoomType().getName())
                            .checkInDate(b.getCheckIn().toString())
                            .checkOutDate(b.getCheckOut().toString())
                            .totalAmount(b.getTotalPrice().toPlainString())
                            .bookingReference("BK-" + b.getId())
                            .build());
        } catch (Exception e) {
            log.warn("Failed to send CONFIRMED notification for booking {}: {}",
                    b.getId(), e.getMessage());
        }
    }

    private void sendCancelledNotificationSafely(Booking b) {
        try {
            notificationService.sendBookingCancelled(
                    NotificationDTOs.BookingCancelledEvent.builder()
                            .bookingId(b.getId())
                            .guestEmail(b.getGuestEmail())
                            .guestName(b.getGuestName())
                            .hotelName(b.getRoomType().getHotel().getName())
                            .bookingReference("BK-" + b.getId())
                            .cancellationReason(b.getCancellationReason())
                            .refundIssued(b.getRefundAmount() != null
                                    && b.getRefundAmount().compareTo(BigDecimal.ZERO) > 0)
                            .refundAmount(b.getRefundAmount() != null
                                    ? b.getRefundAmount().toPlainString() : null)
                            .build());
        } catch (Exception e) {
            log.warn("Failed to send CANCELLED notification for booking {}: {}",
                    b.getId(), e.getMessage());
        }
    }

    private NotificationDTOs.SendRequest buildSendRequest(Booking b, NotificationType type) {
        return NotificationDTOs.SendRequest.builder()
                .recipientEmail(b.getGuestEmail())
                .recipientName(b.getGuestName())
                .type(type)
                .referenceId(b.getId())
                .referenceType(ReferenceType.BOOKING)
                .hotelName(b.getRoomType().getHotel().getName())
                .roomType(b.getRoomType().getName())
                .checkInDate(b.getCheckIn().toString())
                .checkOutDate(b.getCheckOut().toString())
                .totalAmount(b.getTotalPrice().toPlainString())
                .bookingReference("BK-" + b.getId())
                .build();
    }

    private void notifyWaitingListSafely(Booking cancelled) {
        try {
            waitingListService.notifyNextInQueue(
                    cancelled.getRoomType().getId(),
                    cancelled.getCheckIn(),
                    cancelled.getCheckOut()
            );
        } catch (Exception e) {
            log.warn("Failed to notify waiting list for booking {}: {}",
                    cancelled.getId(), e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private — Business helpers
    // ══════════════════════════════════════════════════════════════════════════

    private Booking findOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn))
            throw new InvalidDateRangeException(
                    "checkOut must be strictly after checkIn (minimum 1 night).");
    }

    private void validateDateFilter(String field, LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to))
            throw new InvalidDateRangeException(
                    field + "From must not be after " + field + "To.");
    }

    /**
     * سياسة الـ refund:
     * - 3+ أيام قبل check-in → 100% refund
     * - 1-2 أيام قبل check-in → 50% refund
     * - أقل من يوم         → لا refund
     */
    private BigDecimal calculateRefund(Booking booking) {
        long daysUntilCheckIn = ChronoUnit.DAYS.between(
                LocalDate.now(), booking.getCheckIn());

        if (daysUntilCheckIn >= FULL_REFUND_DAYS)
            return booking.getTotalPrice();

        if (daysUntilCheckIn >= PARTIAL_REFUND_DAYS)
            return booking.getTotalPrice()
                    .multiply(BigDecimal.valueOf(PARTIAL_REFUND_PERCENT))
                    .setScale(2, RoundingMode.HALF_UP);

        return BigDecimal.ZERO;
    }

    private PagedResponse<BookingResponseDTO> toPagedResponse(Page<Booking> page) {
        return new PagedResponse<>(
                page.getContent().stream()
                        .map(b -> bookingMapper.toDto(b, 0L))
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}