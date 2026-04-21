package com.swer313.projectstep1.payment;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * ينشئ payment intent للحجز.
     * يتحقق إن:
     *   - الحجز موجود وبحالة PENDING
     *   - ما في intent نشط (PENDING/SUCCESS) للحجز
     *   - المبلغ يطابق totalPrice الحجز
     */
    PaymentResponseDTO createIntent(PaymentRequestDTO dto);

    /**
     * يحاكي دفع ناجح.
     * بعد النجاح: Payment → SUCCESS + Booking → CONFIRMED + إيميل
     */
    PaymentResponseDTO simulateSuccess(Long id);

    /**
     * يحاكي دفع فاشل.
     * Payment → FAILED + إيميل فشل
     */
    PaymentResponseDTO simulateFailure(Long id, PaymentFailureRequestDTO dto);

    /**
     * يسترجع مبلغ الدفع.
     * يتحقق إن Payment بحالة SUCCESS.
     * يُفوّض التحقق من cancellation policy لـ BookingService.
     * بعد النجاح: Payment → REFUNDED + Booking → CANCELLED + إيميل
     */
    PaymentResponseDTO refund(Long id, PaymentRefundRequestDTO dto);

    // ── Read ──────────────────────────────────────────────────────────────────

    /** جلب payment بالـ ID */
    PaymentResponseDTO getById(Long id);

    /** آخر payment لحجز معيّن */
    PaymentResponseDTO getLatestByBookingId(Long bookingId);

    /** كل محاولات الدفع لحجز معيّن — مرتبة من الأحدث */
    List<PaymentResponseDTO> getHistoryByBookingId(Long bookingId);

    /** قائمة كل الـ payments مع فلترة + pagination */
    PagedResponse<PaymentResponseDTO> getAll(
            Pageable pageable,
            Long bookingId,
            PaymentStatus status,
            PaymentMethod method,
            String currency
    );

    /** جلب payment بالـ transactionReference */
    PaymentResponseDTO getByTransactionReference(String transactionReference);

    /** إحصائيات: عدد لكل status + إجمالي الإيرادات */
    PaymentStatsDTO getStats();
}