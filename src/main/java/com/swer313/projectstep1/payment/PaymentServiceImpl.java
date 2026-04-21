package com.swer313.projectstep1.payment;

import com.swer313.projectstep1.booking.Booking;
import com.swer313.projectstep1.booking.BookingNotFoundException;
import com.swer313.projectstep1.booking.BookingRepository;
import com.swer313.projectstep1.booking.BookingService;
import com.swer313.projectstep1.booking.BookingStatus;
import com.swer313.projectstep1.booking.CancelBookingRequest;
import com.swer313.projectstep1.errors.BadRequestException;
import com.swer313.projectstep1.errors.DuplicatePaymentIntentException;
import com.swer313.projectstep1.errors.InvalidPaymentStateException;
import com.swer313.projectstep1.errors.PaymentNotFoundException;
import com.swer313.projectstep1.errors.RefundNotAllowedException;
import com.swer313.projectstep1.notification.NotificationDTOs.PaymentEvent;
import com.swer313.projectstep1.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private static final String DEFAULT_CURRENCY       = "USD";
    private static final String DEFAULT_PROVIDER       = "MOCK_GATEWAY";
    private static final String DEFAULT_FAILURE_REASON = "Payment was declined by mock gateway";
    private static final String DEFAULT_REFUND_REASON  = "Refund processed by mock gateway";

    private final PaymentRepository   repository;
    private final PaymentMapper       mapper;
    private final BookingRepository   bookingRepository;
    private final BookingService      bookingService;
    private final NotificationService notificationService;

    public PaymentServiceImpl(
            PaymentRepository   repository,
            PaymentMapper       mapper,
            BookingRepository   bookingRepository,
            BookingService      bookingService,
            NotificationService notificationService
    ) {
        this.repository          = repository;
        this.mapper              = mapper;
        this.bookingRepository   = bookingRepository;
        this.bookingService      = bookingService;
        this.notificationService = notificationService;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CREATE INTENT
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public PaymentResponseDTO createIntent(PaymentRequestDTO dto) {
        validateCreateRequest(dto);

        // 1. تحقق إن الحجز موجود
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(dto.getBookingId()));

        // 2. الحجز لازم يكون PENDING
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Cannot create payment intent for booking with status: "
                            + booking.getStatus() + ". Booking must be PENDING."
            );
        }

        // 3. منع intent مكرر — لو في PENDING أو SUCCESS بالفعل
        if (repository.existsByBookingIdAndStatusIn(
                dto.getBookingId(),
                List.of(PaymentStatus.PENDING, PaymentStatus.SUCCESS)
        )) {
            throw new DuplicatePaymentIntentException(dto.getBookingId());
        }

        // 4. تحقق إن المبلغ يطابق totalPrice الحجز بالضبط
        if (dto.getAmount().compareTo(booking.getTotalPrice()) != 0) {
            throw new BadRequestException(
                    "Amount mismatch. Booking total is "
                            + booking.getTotalPrice() + " " + DEFAULT_CURRENCY
                            + ", but provided: " + dto.getAmount()
            );
        }

        // 5. بناء الـ Payment
        Payment payment = mapper.toEntity(dto);
        payment.setProviderName(DEFAULT_PROVIDER);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionReference(generateTransactionReference());

        Payment saved = repository.save(payment);
        log.info("Payment intent created: id={}, bookingId={}, amount={}",
                saved.getId(), saved.getBookingId(), saved.getAmount());

        return mapper.toDto(saved);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SIMULATE SUCCESS
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public PaymentResponseDTO simulateSuccess(Long id) {
        Payment payment = findOrThrow(id);

        // لازم يكون PENDING
        if (!payment.isPending()) {
            throw new InvalidPaymentStateException(
                    "Only PENDING payments can be processed. Current status: "
                            + payment.getStatus()
            );
        }

        // جلب بيانات الحجز للـ notification
        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(payment.getBookingId()));

        // تحديث الـ Payment → SUCCESS
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        payment.setFailureReason(null);
        repository.save(payment);

        // تأكيد الحجز — BookingServiceImpl بيبعث إيميل CONFIRMED تلقائياً
        bookingService.confirmBooking(payment.getBookingId());

        // إيميل نجاح الدفع
        sendPaymentNotificationSafely(payment, booking, true, null, false);

        log.info("Payment SUCCESS: id={}, bookingId={}", payment.getId(), payment.getBookingId());
        return mapper.toDto(payment);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SIMULATE FAILURE
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public PaymentResponseDTO simulateFailure(Long id, PaymentFailureRequestDTO dto) {
        Payment payment = findOrThrow(id);

        // لازم يكون PENDING
        if (!payment.isPending()) {
            throw new InvalidPaymentStateException(
                    "Only PENDING payments can be marked as failed. Current status: "
                            + payment.getStatus()
            );
        }

        // جلب بيانات الحجز للـ notification
        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(payment.getBookingId()));

        String reason = normalizeFailureReason(dto);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        repository.save(payment);

        // إيميل فشل الدفع
        sendPaymentNotificationSafely(payment, booking, false, reason, false);

        log.info("Payment FAILED: id={}, reason={}", payment.getId(), reason);
        return mapper.toDto(payment);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // REFUND
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public PaymentResponseDTO refund(Long id, PaymentRefundRequestDTO dto) {
        Payment payment = findOrThrow(id);

        // تحقق من حالة الـ Payment
        if (payment.isRefunded()) {
            throw new RefundNotAllowedException("Payment #" + id + " is already refunded.");
        }

        if (!payment.isSuccessful()) {
            throw new InvalidPaymentStateException(
                    "Only SUCCESS payments can be refunded. Current status: "
                            + payment.getStatus()
            );
        }

        // جلب بيانات الحجز
        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(payment.getBookingId()));

        String reason = normalizeRefundReason(dto);

        // إلغاء الحجز — BookingServiceImpl يتحقق من الـ cancellation policy.
        // لو ما مسموح (أقل من 12 ساعة) يطرح CancellationNotAllowedException تلقائياً.
        CancelBookingRequest cancelRequest = new CancelBookingRequest();
        cancelRequest.setReason(reason + " (payment #" + id + " refunded)");
        bookingService.cancelBooking(payment.getBookingId(), cancelRequest);

        // تحديث الـ Payment → REFUNDED (بعد نجاح الإلغاء فقط)
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundReason(reason);
        payment.setRefundedAt(LocalDateTime.now());
        repository.save(payment);

        // إيميل الاسترجاع
        sendPaymentNotificationSafely(payment, booking, true, null, true);

        log.info("Payment REFUNDED: id={}, bookingId={}", payment.getId(), payment.getBookingId());
        return mapper.toDto(payment);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // READ
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getById(Long id) {
        return mapper.toDto(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getLatestByBookingId(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new BookingNotFoundException(bookingId);
        }
        return mapper.toDto(
                repository.findTopByBookingIdOrderByCreatedAtDesc(bookingId)
                        .orElseThrow(() -> new PaymentNotFoundException(
                                "No payment found for booking id: " + bookingId))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getHistoryByBookingId(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new BookingNotFoundException(bookingId);
        }
        return repository.findByBookingIdOrderByCreatedAtDesc(bookingId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponseDTO> getAll(
            Pageable pageable,
            Long bookingId,
            PaymentStatus status,
            PaymentMethod method,
            String currency
    ) {
        Specification<Payment> spec = Specification
                .where(PaymentSpecifications.bookingIdEq(bookingId))
                .and(PaymentSpecifications.statusEq(status))
                .and(PaymentSpecifications.methodEq(method))
                .and(PaymentSpecifications.currencyEq(currency));

        Page<Payment> page = repository.findAll(spec, pageable);
        List<PaymentResponseDTO> content = page.getContent().stream()
                .map(mapper::toDto)
                .toList();

        return PagedResponse.from(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getByTransactionReference(String transactionReference) {
        if (transactionReference == null || transactionReference.isBlank()) {
            throw new BadRequestException("transactionReference is required");
        }
        return mapper.toDto(
                repository.findByTransactionReference(transactionReference.trim())
                        .orElseThrow(() -> new PaymentNotFoundException(
                                "Payment not found with transactionReference: "
                                        + transactionReference))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatsDTO getStats() {
        List<Object[]> grouped = repository.countGroupedByStatus();
        BigDecimal totalRevenue = repository.sumSuccessfulPayments();

        Map<PaymentStatus, Long> counts = new HashMap<>();
        for (Object[] row : grouped) {
            counts.put((PaymentStatus) row[0], (Long) row[1]);
        }

        long total      = repository.count();
        long pending    = counts.getOrDefault(PaymentStatus.PENDING,  0L);
        long successful = counts.getOrDefault(PaymentStatus.SUCCESS,  0L);
        long failed     = counts.getOrDefault(PaymentStatus.FAILED,   0L);
        long refunded   = counts.getOrDefault(PaymentStatus.REFUNDED, 0L);

        return new PaymentStatsDTO(total, pending, successful, failed, refunded, totalRevenue);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private helpers
    // ══════════════════════════════════════════════════════════════════════════

    private Payment findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private void validateCreateRequest(PaymentRequestDTO dto) {
        if (dto == null)
            throw new BadRequestException("Request body is required");
        if (dto.getBookingId() == null)
            throw new BadRequestException("bookingId is required");
        if (dto.getAmount() == null)
            throw new BadRequestException("amount is required");
        if (dto.getAmount().signum() <= 0)
            throw new BadRequestException("amount must be greater than 0");
        if (dto.getCurrency() != null && !dto.getCurrency().isBlank()
                && dto.getCurrency().trim().length() != 3)
            throw new BadRequestException("currency must be a 3-letter ISO code (e.g. USD)");
    }

    private String normalizeFailureReason(PaymentFailureRequestDTO dto) {
        return (dto == null || dto.getReason() == null || dto.getReason().isBlank())
                ? DEFAULT_FAILURE_REASON : dto.getReason().trim();
    }

    private String normalizeRefundReason(PaymentRefundRequestDTO dto) {
        return (dto == null || dto.getReason() == null || dto.getReason().isBlank())
                ? DEFAULT_REFUND_REASON : dto.getReason().trim();
    }

    /**
     * فشل الـ notification ما يفشّل الـ payment —
     * نفس pattern المستخدم في BookingServiceImpl.
     */
    private void sendPaymentNotificationSafely(
            Payment payment, Booking booking,
            boolean success, String failureReason, boolean isRefund
    ) {
        try {
            notificationService.sendPaymentNotification(
                    PaymentEvent.builder()
                            .paymentId(payment.getId())
                            .bookingId(booking.getId())
                            .guestEmail(booking.getGuestEmail())
                            .guestName(booking.getGuestName())
                            .amount(payment.getCurrency() + " " + payment.getAmount())
                            .paymentMethod(payment.getMethod().name())
                            .success(success)
                            .failureReason(failureReason)
                            .refund(isRefund)
                            .build()
            );
        } catch (Exception e) {
            log.warn("Failed to send payment notification for payment {}: {}",
                    payment.getId(), e.getMessage());
        }
    }

    private String generateTransactionReference() {
        return "pay_" + UUID.randomUUID().toString().replace("-", "");
    }
}