package com.swer313.projectstep1.notification;

import org.springframework.stereotype.Component;

/**
 * Builds HTML email bodies from structured data.
 * In a real project these could be Thymeleaf templates,
 * but keeping them self-contained keeps the monolith simple.
 */
@Component
public class EmailTemplateBuilder {

    private static final String BRAND = "TourismStay";
    private static final String PRIMARY_COLOR = "#1a73e8";
    private static final String INFO_COLOR = "#3498db";
    private static final String SUCCESS_COLOR = "#28a745";
    private static final String DANGER_COLOR  = "#dc3545";
    private static final String WARNING_COLOR = "#ffc107";

    // ─── Subject lines ─────────────────────────────────────────────────────────

    public String buildSubject(NotificationType type, NotificationDTOs.SendRequest req) {
        return switch (type) {
            case BOOKING_CONFIRMED  -> "✅ Booking Confirmed – " + req.getBookingReference();
            case BOOKING_CANCELLED  -> "❌ Booking Cancelled – " + req.getBookingReference();
            case BOOKING_PENDING    -> "⏳ Booking Pending – " + req.getBookingReference();
            case PAYMENT_SUCCESS    -> "💳 Payment Received – " + req.getTotalAmount();
            case BOOKING_REMINDER -> "⏰ Reminder: Check-in in 2 days – " + req.getBookingReference();
            case PAYMENT_FAILED     -> "⚠️ Payment Failed – Action Required";
            case PAYMENT_REFUNDED   -> "💰 Refund Processed – " + req.getTotalAmount();
            case WELCOME_EMAIL      -> "👋 Welcome to " + BRAND + "!";
            case PASSWORD_RESET     -> "🔐 Password Reset Request";
            case REVIEW_REMINDER -> "⭐ How was your stay at " + req.getHotelName() + "?";
            case ROOM_AVAILABLE -> "🎉 Room Available! Book now for " + req.getHotelName();
            case CUSTOM             -> "Message from " + BRAND;
        };
    }

    // ─── Body builders ─────────────────────────────────────────────────────────

    public String buildBody(NotificationType type, NotificationDTOs.SendRequest req) {
        return switch (type) {
            case BOOKING_CONFIRMED -> bookingConfirmedBody(req);
            case BOOKING_CANCELLED -> bookingCancelledBody(req);
            case BOOKING_PENDING   -> bookingPendingBody(req);
            case PAYMENT_SUCCESS   -> paymentSuccessBody(req);
            case BOOKING_REMINDER -> bookingReminderBody(req);
            case PAYMENT_FAILED    -> paymentFailedBody(req);
            case PAYMENT_REFUNDED  -> paymentRefundedBody(req);
            case WELCOME_EMAIL     -> welcomeBody(req);
            case PASSWORD_RESET    -> passwordResetBody(req);
            case REVIEW_REMINDER -> reviewReminderBody(req);
            case ROOM_AVAILABLE -> roomAvailableBody(req);
            case CUSTOM            -> "<p>Please see the message in the subject line.</p>";
        };
    }

    // ─── Templates ─────────────────────────────────────────────────────────────

    private String bookingConfirmedBody(NotificationDTOs.SendRequest req) {
        return wrap(req.getRecipientName(), SUCCESS_COLOR, "Booking Confirmed!", """
                <p>Great news! Your booking has been confirmed. Here are your details:</p>
                """ + detailTable(req) + """
                <p>We look forward to welcoming you!</p>
                <p style="margin-top:20px;">
                  <a href="#" style="background:%s;color:#fff;padding:12px 24px;
                     border-radius:4px;text-decoration:none;font-weight:bold;">
                    View Booking
                  </a>
                </p>
                """.formatted(PRIMARY_COLOR));
    }

    private String bookingCancelledBody(NotificationDTOs.SendRequest req) {
        String extra = req.getCancellationReason() != null
                ? "<p><strong>Reason:</strong> " + req.getCancellationReason() + "</p>"
                : "";
        return wrap(req.getRecipientName(), DANGER_COLOR, "Booking Cancelled", """
                <p>We're sorry to inform you that your booking has been cancelled.</p>
                <p><strong>Hotel:</strong> %s</p>
                <p><strong>Booking Reference:</strong> %s</p>
                %s
                <p>If you did not request this cancellation, please contact our support team immediately.</p>
                """.formatted(req.getHotelName(), req.getBookingReference(), extra));
    }

    private String bookingPendingBody(NotificationDTOs.SendRequest req) {
        return wrap(req.getRecipientName(), WARNING_COLOR, "Booking Pending", """
                <p>Your booking is currently <strong>pending confirmation</strong>.</p>
                """ + detailTable(req) + """
                <p>You will receive another email once the booking is confirmed.</p>
                """);
    }

    private String paymentSuccessBody(NotificationDTOs.SendRequest req) {
        return wrap(req.getRecipientName(), SUCCESS_COLOR, "Payment Successful", """
                <p>Your payment has been processed successfully.</p>
                <table style="border-collapse:collapse;width:100%%;margin:16px 0;">
                  <tr><td style="padding:8px;color:#666;">Amount</td>
                      <td style="padding:8px;font-weight:bold;">%s</td></tr>
                  <tr style="background:#f9f9f9;">
                      <td style="padding:8px;color:#666;">Payment Method</td>
                      <td style="padding:8px;">%s</td></tr>
                  <tr><td style="padding:8px;color:#666;">Booking Reference</td>
                      <td style="padding:8px;">%s</td></tr>
                </table>
                <p>Thank you for your payment!</p>
                """.formatted(
                req.getTotalAmount(),
                req.getPaymentMethod() != null ? req.getPaymentMethod() : "N/A",
                req.getBookingReference() != null ? req.getBookingReference() : "N/A"));
    }
    private String bookingReminderBody(NotificationDTOs.SendRequest req) {
        return wrap(req.getRecipientName(), INFO_COLOR, "Booking Reminder", """
            <p>This is a friendly <strong>reminder</strong> for your upcoming booking.</p>
            """ + detailTable(req) + """
            <p>We look forward to welcoming you soon!</p>
            """);
    }
    private String paymentFailedBody(NotificationDTOs.SendRequest req) {
        return wrap(req.getRecipientName(), DANGER_COLOR, "Payment Failed", """
                <p>Unfortunately, we were unable to process your payment.</p>
                <p>Please update your payment method and try again to keep your booking active.</p>
                <p style="margin-top:20px;">
                  <a href="#" style="background:%s;color:#fff;padding:12px 24px;
                     border-radius:4px;text-decoration:none;font-weight:bold;">
                    Retry Payment
                  </a>
                </p>
                <p style="color:#999;font-size:13px;">
                  If you continue to experience issues, please contact support.
                </p>
                """.formatted(DANGER_COLOR));

    }
    private String reviewReminderBody(NotificationDTOs.SendRequest req) {
        return wrap(req.getRecipientName(), PRIMARY_COLOR, "How Was Your Stay?", """
            <p>We hope you enjoyed your recent stay at <strong>%s</strong>!</p>
            <p>Your feedback means a lot to us and helps other travelers make informed decisions.</p>
            <p>Booking Reference: <strong>%s</strong></p>
            <p style="margin-top:20px;">
              <a href="#" style="background:%s;color:#fff;padding:12px 24px;
                 border-radius:4px;text-decoration:none;font-weight:bold;">
                Write a Review
              </a>
            </p>
            <p style="color:#999;font-size:13px;">
              You have 30 days from checkout to submit your review.
            </p>
            """.formatted(
                req.getHotelName()        != null ? req.getHotelName()        : "N/A",
                req.getBookingReference() != null ? req.getBookingReference() : "N/A",
                PRIMARY_COLOR));
    }

    private String paymentRefundedBody(NotificationDTOs.SendRequest req) {
        return wrap(req.getRecipientName(), PRIMARY_COLOR, "Refund Processed", """
                <p>Your refund of <strong>%s</strong> has been processed.</p>
                <p>Please allow 5–10 business days for the amount to appear in your account,
                   depending on your bank.</p>
                """.formatted(req.getTotalAmount() != null ? req.getTotalAmount() : "N/A"));
    }

    private String welcomeBody(NotificationDTOs.SendRequest req) {
        return wrap(req.getRecipientName(), PRIMARY_COLOR, "Welcome to " + BRAND, """
                <p>We're thrilled to have you on board! Start exploring our curated selection
                   of hotels and make your next trip unforgettable.</p>
                <p style="margin-top:20px;">
                  <a href="#" style="background:%s;color:#fff;padding:12px 24px;
                     border-radius:4px;text-decoration:none;font-weight:bold;">
                    Browse Hotels
                  </a>
                </p>
                """.formatted(PRIMARY_COLOR));
    }

    private String passwordResetBody(NotificationDTOs.SendRequest req) {
        return wrap(req.getRecipientName(), WARNING_COLOR, "Password Reset", """
                <p>We received a request to reset your password.</p>
                <p style="margin-top:20px;">
                  <a href="#" style="background:%s;color:#fff;padding:12px 24px;
                     border-radius:4px;text-decoration:none;font-weight:bold;">
                    Reset Password
                  </a>
                </p>
                <p style="color:#999;font-size:13px;">
                  This link expires in 1 hour. If you did not request a reset, ignore this email.
                </p>
                """.formatted(WARNING_COLOR));
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private String detailTable(NotificationDTOs.SendRequest req) {
        return """
                <table style="border-collapse:collapse;width:100%%;margin:16px 0;">
                  <tr><td style="padding:8px;color:#666;">Hotel</td>
                      <td style="padding:8px;font-weight:bold;">%s</td></tr>
                  <tr style="background:#f9f9f9;">
                      <td style="padding:8px;color:#666;">Room Type</td>
                      <td style="padding:8px;">%s</td></tr>
                  <tr><td style="padding:8px;color:#666;">Check-in</td>
                      <td style="padding:8px;">%s</td></tr>
                  <tr style="background:#f9f9f9;">
                      <td style="padding:8px;color:#666;">Check-out</td>
                      <td style="padding:8px;">%s</td></tr>
                  <tr><td style="padding:8px;color:#666;">Total Amount</td>
                      <td style="padding:8px;font-weight:bold;">%s</td></tr>
                  <tr style="background:#f9f9f9;">
                      <td style="padding:8px;color:#666;">Booking Ref</td>
                      <td style="padding:8px;">%s</td></tr>
                </table>
                """.formatted(
                req.getHotelName()         != null ? req.getHotelName()         : "N/A",
                req.getRoomType()          != null ? req.getRoomType()           : "N/A",
                req.getCheckInDate()       != null ? req.getCheckInDate()        : "N/A",
                req.getCheckOutDate()      != null ? req.getCheckOutDate()       : "N/A",
                req.getTotalAmount()       != null ? req.getTotalAmount()        : "N/A",
                req.getBookingReference()  != null ? req.getBookingReference()   : "N/A");
    }
    private String roomAvailableBody(NotificationDTOs.SendRequest req) {
        return wrap(req.getRecipientName(), SUCCESS_COLOR, "A Room Is Now Available!", """
            <p>Great news! A room you were waiting for is now available.</p>
            <table style="border-collapse:collapse;width:100%%;margin:16px 0;">
              <tr><td style="padding:8px;color:#666;">Hotel</td>
                  <td style="padding:8px;font-weight:bold;">%s</td></tr>
              <tr style="background:#f9f9f9;">
                  <td style="padding:8px;color:#666;">Room Type</td>
                  <td style="padding:8px;">%s</td></tr>
              <tr><td style="padding:8px;color:#666;">Check-in</td>
                  <td style="padding:8px;">%s</td></tr>
              <tr style="background:#f9f9f9;">
                  <td style="padding:8px;color:#666;">Check-out</td>
                  <td style="padding:8px;">%s</td></tr>
            </table>
            <p style="color:#dc3545;font-weight:bold;">
              ⚠️ This room will be held for 24 hours. Book now before it's taken!
            </p>
            <p style="margin-top:20px;">
              <a href="#" style="background:%s;color:#fff;padding:12px 24px;
                 border-radius:4px;text-decoration:none;font-weight:bold;">
                Book Now
              </a>
            </p>
            """.formatted(
                req.getHotelName()   != null ? req.getHotelName()   : "N/A",
                req.getRoomType()    != null ? req.getRoomType()     : "N/A",
                req.getCheckInDate() != null ? req.getCheckInDate()  : "N/A",
                req.getCheckOutDate()!= null ? req.getCheckOutDate() : "N/A",
                SUCCESS_COLOR));
    }

    private String wrap(String name, String headerColor, String title, String content) {
        return """
                <!DOCTYPE html>
                <html><head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0;">
                  <div style="max-width:600px;margin:40px auto;background:#fff;border-radius:8px;
                              overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1);">
                    <div style="background:%s;color:#fff;padding:32px 40px;">
                      <h1 style="margin:0;font-size:24px;">%s</h1>
                      <p style="margin:8px 0 0;opacity:.9;">%s</p>
                    </div>
                    <div style="padding:32px 40px;">
                      <p>Dear <strong>%s</strong>,</p>
                      %s
                      <hr style="border:none;border-top:1px solid #eee;margin:24px 0;">
                      <p style="color:#999;font-size:12px;">
                        This is an automated email from %s. Please do not reply directly.
                      </p>
                    </div>
                  </div>
                </body></html>
                """.formatted(headerColor, BRAND, title, name, content, BRAND);
    }
}
