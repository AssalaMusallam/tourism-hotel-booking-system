package com.swer313.projectstep1.payment;

import jakarta.validation.constraints.Size;

public class PaymentFailureRequestDTO {

    @Size(max = 500, message = "reason must be at most 500 characters")
    private String reason;

    public PaymentFailureRequestDTO() {}
    public String getReason()             { return reason; }
    public void setReason(String reason)  { this.reason = reason; }
}