package com.swer313.projectstep1.payment;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_booking_id", columnList = "booking_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_method", columnList = "method"),
                @Index(name = "idx_payment_created_at", columnList = "created_at"),
                @Index(name = "idx_payment_transaction_ref", columnList = "transaction_reference", unique = true)
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Keep only bookingId instead of @ManyToOne with Booking
    // عشان ال payment module يضل مستقل، ويكون أسهل لو لاحقًا صار في microservices
    @NotNull
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    // Minimum allowed payment amount = 0.01
    // يعني ما بصير payment بمبلغ صفر أو سالب
    @NotNull
    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private String currency = "USD";

    // Store enum as String, not ordinal
    // أفضل لأنه أوضح بالداتابيس وما بخرب لو تغير ترتيب القيم داخل enum
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method = PaymentMethod.MOCK_CARD;

    // New payment starts as PENDING by default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName = "MOCK_GATEWAY";

    // Unique reference for each payment transaction
    // مهم للتتبع والتمييز بين كل عملية دفع والثانية
    @Column(name = "transaction_reference", nullable = false, unique = true, length = 64)
    private String transactionReference;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    // set when payment becomes SUCCESS
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // set when payment becomes REFUNDED
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    // Auto-filled once when row is first created
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Auto-updated on every change
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Payment() {}

    // Helper methods to make service logic cleaner
    // بدل ما كل مرة نعمل مقارنة مباشرة مع status
    public boolean isPending()    { return status == PaymentStatus.PENDING; }
    public boolean isSuccessful() { return status == PaymentStatus.SUCCESS; }
    public boolean isRefunded()   { return status == PaymentStatus.REFUNDED; }

    public Long getId()                                         { return id; }
    public Long getBookingId()                                  { return bookingId; }
    public void setBookingId(Long bookingId)                    { this.bookingId = bookingId; }
    public BigDecimal getAmount()                               { return amount; }
    public void setAmount(BigDecimal amount)                    { this.amount = amount; }
    public String getCurrency()                                 { return currency; }
    public void setCurrency(String currency)                    { this.currency = currency; }
    public PaymentMethod getMethod()                            { return method; }
    public void setMethod(PaymentMethod method)                 { this.method = method; }
    public PaymentStatus getStatus()                            { return status; }
    public void setStatus(PaymentStatus status)                 { this.status = status; }
    public String getProviderName()                             { return providerName; }
    public void setProviderName(String providerName)            { this.providerName = providerName; }
    public String getTransactionReference()                     { return transactionReference; }
    public void setTransactionReference(String ref)             { this.transactionReference = ref; }
    public String getFailureReason()                            { return failureReason; }
    public void setFailureReason(String failureReason)          { this.failureReason = failureReason; }
    public String getRefundReason()                             { return refundReason; }
    public void setRefundReason(String refundReason)            { this.refundReason = refundReason; }
    public LocalDateTime getPaidAt()                            { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt)                 { this.paidAt = paidAt; }
    public LocalDateTime getRefundedAt()                        { return refundedAt; }
    public void setRefundedAt(LocalDateTime refundedAt)         { this.refundedAt = refundedAt; }
    public LocalDateTime getCreatedAt()                         { return createdAt; }
    public LocalDateTime getUpdatedAt()                         { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        Payment other = (Payment) o;

        // Two Payment objects are considered the same if they have the same DB id
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}