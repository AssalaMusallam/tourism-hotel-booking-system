package com.swer313.projectstep1.booking;

import jakarta.validation.constraints.NotBlank;

public class CancelBookingRequest {

    @NotBlank(message = "Cancellation reason is required")
    private String reason;

    public String getReason()             { return reason; }
    public void setReason(String reason)  { this.reason = reason; }
}