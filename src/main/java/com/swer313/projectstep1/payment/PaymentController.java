package com.swer313.projectstep1.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "7. Payments", description = "Create and manage payments")
public class PaymentController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE     = 50;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "bookingId", "amount", "currency", "method",
            "status", "createdAt", "updatedAt", "paidAt", "refundedAt"
    );

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * POST /api/payments/intents
     * ينشئ payment intent للحجز.
     *
     * Body: { "bookingId": 1, "amount": 250.00, "currency": "USD" }
     */
    @Operation(summary = "Create payment intent")
    @ApiResponse(responseCode = "201", description = "Payment intent created successfully")
    @PreAuthorize("hasRole('ADMIN') or @paymentSecurityService.canAccessBookingPayment(#dto.bookingId, authentication)")
    @PostMapping("/intents")
    public ResponseEntity<PaymentResponseDTO> createIntent(
            @Valid @RequestBody PaymentRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createIntent(dto));
    }

    /**
     * POST /api/payments/{id}/simulate-success
     * يحاكي دفع ناجح → Payment: SUCCESS + Booking: CONFIRMED
     */
    @Operation(summary = "Simulate successful payment")
    @PreAuthorize("hasRole('ADMIN') or @paymentSecurityService.canAccessPayment(#id, authentication)")
    @PostMapping("/{id}/simulate-success")
    public ResponseEntity<PaymentResponseDTO> simulateSuccess(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.simulateSuccess(id));
    }

    /**
     * POST /api/payments/{id}/simulate-failure
     * يحاكي دفع فاشل → Payment: FAILED
     *
     * Body (اختياري): { "reason": "Insufficient funds" }
     */
    @Operation(summary = "Simulate failed payment")
    @PreAuthorize("hasRole('ADMIN') or @paymentSecurityService.canAccessPayment(#id, authentication)")
    @PostMapping("/{id}/simulate-failure")
    public ResponseEntity<PaymentResponseDTO> simulateFailure(
            @PathVariable Long id,
            @RequestBody(required = false) PaymentFailureRequestDTO dto
    ) {
        return ResponseEntity.ok(paymentService.simulateFailure(id, dto));
    }

    /**
     * POST /api/payments/{id}/refund
     * يسترجع المبلغ → Payment: REFUNDED + Booking: CANCELLED
     *
     * Body (اختياري): { "reason": "Guest changed plans" }
     */
    @Operation(summary = "Refund payment")
    @PreAuthorize("hasRole('ADMIN') or @paymentSecurityService.canAccessPayment(#id, authentication)")
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponseDTO> refund(
            @PathVariable Long id,
            @RequestBody(required = false) PaymentRefundRequestDTO dto
    ) {
        return ResponseEntity.ok(paymentService.refund(id, dto));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * GET /api/payments
     * قائمة كل الـ payments مع فلترة + pagination.
     *
     * Query params: bookingId, status, method, currency, page, size, sort
     * مثال: GET /api/payments?status=SUCCESS&page=0&size=5&sort=createdAt,desc
     */
    @Operation(summary = "Get all payments with filters")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PagedResponse<PaymentResponseDTO>> getAll(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentMethod method,
            @RequestParam(required = false) String currency,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(
                paymentService.getAll(pageable, bookingId, status, method, currency)
        );
    }

    /**
     * GET /api/payments/{id}
     * جلب payment بالـ ID.
     */
    @Operation(summary = "Get payment by ID")
    @PreAuthorize("hasRole('ADMIN') or @paymentSecurityService.canAccessPayment(#id, authentication)")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    /**
     * GET /api/payments/booking/{bookingId}
     * آخر payment لحجز معيّن.
     */
    @Operation(summary = "Get latest payment by booking ID")
    @PreAuthorize("hasRole('ADMIN') or @paymentSecurityService.canAccessBookingPayment(#bookingId, authentication)")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponseDTO> getLatestByBookingId(
            @PathVariable Long bookingId
    ) {
        return ResponseEntity.ok(paymentService.getLatestByBookingId(bookingId));
    }

    /**
     * GET /api/payments/booking/{bookingId}/history
     * كل محاولات الدفع لحجز معيّن — مرتبة من الأحدث.
     * مفيد لو فشل الدفع أكثر من مرة ثم نجح.
     */
    @Operation(summary = "Get payment history by booking ID")
    @PreAuthorize("hasRole('ADMIN') or @paymentSecurityService.canAccessBookingPayment(#bookingId, authentication)")
    @GetMapping("/booking/{bookingId}/history")
    public ResponseEntity<List<PaymentResponseDTO>> getHistoryByBookingId(
            @PathVariable Long bookingId
    ) {
        return ResponseEntity.ok(paymentService.getHistoryByBookingId(bookingId));
    }

    /**
     * GET /api/payments/by-reference?ref=pay_abc123...
     * جلب payment بالـ transactionReference.
     */
    @Operation(summary = "Get payment by transaction reference")
    @PreAuthorize("hasRole('ADMIN') or @paymentSecurityService.canAccessPaymentByReference(#ref, authentication)")
    @GetMapping("/by-reference")
    public ResponseEntity<PaymentResponseDTO> getByTransactionReference(
            @RequestParam String ref
    ) {
        return ResponseEntity.ok(paymentService.getByTransactionReference(ref));
    }

    /**
     * GET /api/payments/stats
     * إحصائيات: total, pending, successful, failed, refunded, totalRevenue.
     */
    @Operation(summary = "Get payments statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<PaymentStatsDTO> getStats() {
        return ResponseEntity.ok(paymentService.getStats());
    }

    // ── Private: Pageable Builder ─────────────────────────────────────────────

    private Pageable buildPageable(int page, int size, String sort) {
        if (page < 0)
            throw new IllegalArgumentException("page must be >= 0");
        if (size <= 0)
            throw new IllegalArgumentException("size must be > 0");
        if (size > MAX_SIZE)
            throw new IllegalArgumentException("size must be <= " + MAX_SIZE);

        return PageRequest.of(page, size, parseSort(sort));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank())
            return Sort.by(Sort.Direction.DESC, "id");

        String[] parts = sort.split(",");
        String field = parts[0].trim();

        if (!ALLOWED_SORT_FIELDS.contains(field))
            throw new IllegalArgumentException("Invalid sort field: " + field
                    + ". Allowed: " + ALLOWED_SORT_FIELDS);

        Sort.Direction direction = Sort.Direction.DESC;
        if (parts.length > 1) {
            String dir = parts[1].trim().toLowerCase();
            if ("asc".equals(dir)) direction = Sort.Direction.ASC;
            else if (!"desc".equals(dir))
                throw new IllegalArgumentException("Invalid sort direction: " + parts[1]);
        }

        return Sort.by(direction, field);
    }
}