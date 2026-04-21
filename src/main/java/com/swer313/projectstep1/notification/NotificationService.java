package com.swer313.projectstep1.notification;

import com.swer313.projectstep1.notification.NotificationDTOs.*;

import java.util.List;
import java.util.Map;

/**
 * Public API for the notification module.
 * Other modules (booking, payment) call these methods to trigger notifications.
 */
public interface NotificationService {

    // ─── Send methods ──────────────────────────────────────────────────────────

    /**
     * Main entry point — builds subject + body from template then sends.
     */
    NotificationResponse send(SendRequest request);

    /**
     * Convenience — booking confirmed notification.
     * Called by BookingServiceImpl after successful booking.
     */
    NotificationResponse sendBookingConfirmed(BookingConfirmedEvent event);

    /**
     * Convenience — booking cancelled notification.
     * Called by BookingServiceImpl after cancellation.
     */
    NotificationResponse sendBookingCancelled(BookingCancelledEvent event);

    /**
     * Convenience — payment notification (success / failed / refund).
     * Called by PaymentServiceImpl.
     */
    NotificationResponse sendPaymentNotification(PaymentEvent event);

    /**
     * Admin-only — send a fully custom email (subject + body provided manually).
     */
    NotificationResponse sendCustom(CustomRequest request);

    /**
     * Bridge method — called by BookingServiceImpl with raw subject + body.
     * No template building needed.
     */
    NotificationResponse sendBookingNotification(String guestEmail,
                                                 NotificationType type,
                                                 String subject,
                                                 String body);

    // ─── Query methods ──────────────────────────────────────────────────────────

    NotificationResponse getById(Long id);

    PagedNotificationResponse getByEmail(String email, int page, int size);

    PagedNotificationResponse getAll(int page, int size);

    PagedNotificationResponse getByStatus(NotificationStatus status, int page, int size);

    List<NotificationResponse> getByReference(Long referenceId, ReferenceType type);

    /**
     * Returns stats: total, sent, failed, pending, retryScheduled,
     * permanentlyFailed, sentLast24h.
     */
    Map<String, Long> getStats();

    // ─── Retry ─────────────────────────────────────────────────────────────────

    /**
     * Admin-only — manually retry a failed notification.
     * Throws IllegalStateException if already SENT or max retries reached.
     */
    NotificationResponse retryManually(Long id);

    /**
     * Scheduled — runs every minute automatically.
     * Retries all RETRY_SCHEDULED notifications that are due.
     */
    void processRetryQueue();
}