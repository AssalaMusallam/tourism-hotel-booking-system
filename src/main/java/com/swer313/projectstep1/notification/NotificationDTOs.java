package com.swer313.projectstep1.notification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationDTOs {

    // ─── Send Request ────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendRequest {

        @NotBlank(message = "Recipient email is required")
        @Email(message = "Invalid email format")
        private String recipientEmail;

        @NotBlank(message = "Recipient name is required")
        private String recipientName;

        @NotNull(message = "Notification type is required")
        private NotificationType type;

        private Long referenceId;
        private ReferenceType referenceType;

        // Optional template data
        private String hotelName;
        private String roomType;
        private String checkInDate;
        private String checkOutDate;
        private String totalAmount;
        private String bookingReference;
        private String cancellationReason;
        private String paymentMethod;
    }

    // ─── Custom Request (Admin only) ─────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomRequest {

        @NotBlank(message = "Recipient email is required")
        @Email(message = "Invalid email format")
        private String recipientEmail;

        @NotBlank(message = "Recipient name is required")
        private String recipientName;

        @NotBlank(message = "Subject is required")
        private String subject;

        @NotBlank(message = "Body is required")
        private String body;
    }

    // ─── Response DTO ─────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponse {
        private Long id;
        private String recipientEmail;
        private String recipientName;
        private NotificationType type;
        private NotificationStatus status;
        private String subject;
        private Long referenceId;
        private ReferenceType referenceType;
        private int retryCount;
        private String errorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime sentAt;
    }

    // ─── Paged Response ───────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagedNotificationResponse {
        private List<NotificationResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean last;
    }

    // ─── Internal Event DTOs ─────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingConfirmedEvent {

        @NotNull(message = "Booking ID is required")
        private Long bookingId;

        @NotBlank(message = "Guest email is required")
        @Email
        private String guestEmail;

        @NotBlank(message = "Guest name is required")
        private String guestName;

        private String hotelName;
        private String roomType;
        private String checkInDate;
        private String checkOutDate;
        private String totalAmount;
        private String bookingReference;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingCancelledEvent {

        @NotNull(message = "Booking ID is required")
        private Long bookingId;

        @NotBlank(message = "Guest email is required")
        @Email
        private String guestEmail;

        @NotBlank(message = "Guest name is required")
        private String guestName;

        private String hotelName;
        private String bookingReference;
        private String cancellationReason;
        private boolean refundIssued;
        private String refundAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentEvent {

        @NotNull(message = "Payment ID is required")
        private Long paymentId;

        private Long bookingId;

        @NotBlank(message = "Guest email is required")
        @Email
        private String guestEmail;

        @NotBlank(message = "Guest name is required")
        private String guestName;

        private String amount;
        private String paymentMethod;
        private boolean success;
        private String failureReason;
        private boolean refund;
    }
}
