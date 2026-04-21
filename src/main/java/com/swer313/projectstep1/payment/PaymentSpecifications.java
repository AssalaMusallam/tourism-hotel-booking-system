package com.swer313.projectstep1.payment;

import org.springframework.data.jpa.domain.Specification;

public class PaymentSpecifications {

    private PaymentSpecifications() {}

    public static Specification<Payment> bookingIdEq(Long bookingId) {
        return (root, query, cb) ->
                bookingId == null ? null : cb.equal(root.get("bookingId"), bookingId);
    }

    public static Specification<Payment> statusEq(PaymentStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Payment> methodEq(PaymentMethod method) {
        return (root, query, cb) ->
                method == null ? null : cb.equal(root.get("method"), method);
    }

    public static Specification<Payment> currencyEq(String currency) {
        return (root, query, cb) -> {
            if (currency == null || currency.isBlank()) return null;
            return cb.equal(cb.upper(root.get("currency")), currency.trim().toUpperCase());
        };
    }
}