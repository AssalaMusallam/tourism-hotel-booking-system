package com.swer313.projectstep1.payment;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponseDTO {

    private final Long          id;
    private final Long          bookingId;
    private final BigDecimal    amount;
    private final String        currency;
    private final PaymentMethod method;
    private final PaymentStatus status;
    private final String        providerName;
    private final String        transactionReference;
    private final String        failureReason;   // null إلا لو FAILED
    private final String        refundReason;    // null إلا لو REFUNDED
    private final LocalDateTime paidAt;          // null إلا لو SUCCESS
    private final LocalDateTime refundedAt;      // null إلا لو REFUNDED
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PaymentResponseDTO(
            Long id, Long bookingId,
            BigDecimal amount, String currency,
            PaymentMethod method, PaymentStatus status,
            String providerName, String transactionReference,
            String failureReason, String refundReason,
            LocalDateTime paidAt, LocalDateTime refundedAt,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this.id                   = id;
        this.bookingId            = bookingId;
        this.amount               = amount;
        this.currency             = currency;
        this.method               = method;
        this.status               = status;
        this.providerName         = providerName;
        this.transactionReference = transactionReference;
        this.failureReason        = failureReason;
        this.refundReason         = refundReason;
        this.paidAt               = paidAt;
        this.refundedAt           = refundedAt;
        this.createdAt            = createdAt;
        this.updatedAt            = updatedAt;
    }

    public Long          getId()                   { return id; }
    public Long          getBookingId()            { return bookingId; }
    public BigDecimal    getAmount()               { return amount; }
    public String        getCurrency()             { return currency; }
    public PaymentMethod getMethod()               { return method; }
    public PaymentStatus getStatus()               { return status; }
    public String        getProviderName()         { return providerName; }
    public String        getTransactionReference() { return transactionReference; }
    public String        getFailureReason()        { return failureReason; }
    public String        getRefundReason()         { return refundReason; }
    public LocalDateTime getPaidAt()               { return paidAt; }
    public LocalDateTime getRefundedAt()           { return refundedAt; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
    public LocalDateTime getUpdatedAt()            { return updatedAt; }
}