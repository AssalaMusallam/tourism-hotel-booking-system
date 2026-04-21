package com.swer313.projectstep1.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request لإنشاء payment intent.
 * المبلغ بيتحقق منه الـ service إنه يطابق totalPrice الحجز.
 */
public class PaymentRequestDTO {

    @NotNull(message = "bookingId is required")
    private Long bookingId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    /** اختياري — إذا null أو blank يُستخدم USD */
    @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code (e.g. USD)")
    private String currency;

    /** اختياري — إذا null يُستخدم MOCK_CARD */
    private PaymentMethod method;

    public PaymentRequestDTO() {}

    public Long getBookingId()                  { return bookingId; }
    public void setBookingId(Long bookingId)    { this.bookingId = bookingId; }
    public BigDecimal getAmount()               { return amount; }
    public void setAmount(BigDecimal amount)    { this.amount = amount; }
    public String getCurrency()                 { return currency; }
    public void setCurrency(String currency)    { this.currency = currency; }
    public PaymentMethod getMethod()            { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
}