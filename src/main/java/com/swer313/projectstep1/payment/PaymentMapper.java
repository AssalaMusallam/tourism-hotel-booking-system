package com.swer313.projectstep1.payment;

import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(PaymentRequestDTO dto) {
        if (dto == null) return null;

        Payment payment = new Payment();
        payment.setBookingId(dto.getBookingId());
        payment.setAmount(dto.getAmount());
        payment.setCurrency(
                dto.getCurrency() != null && !dto.getCurrency().isBlank()
                        ? dto.getCurrency().trim().toUpperCase()
                        : "USD"
        );
        payment.setMethod(
                dto.getMethod() != null ? dto.getMethod() : PaymentMethod.MOCK_CARD
        );
        return payment;
    }

    public PaymentResponseDTO toDto(Payment p) {
        if (p == null) return null;

        return new PaymentResponseDTO(
                p.getId(),
                p.getBookingId(),
                p.getAmount(),
                p.getCurrency(),
                p.getMethod(),
                p.getStatus(),
                p.getProviderName(),
                p.getTransactionReference(),
                p.getFailureReason(),
                p.getRefundReason(),
                p.getPaidAt(),
                p.getRefundedAt(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}