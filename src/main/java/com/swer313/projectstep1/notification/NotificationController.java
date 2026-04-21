package com.swer313.projectstep1.notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Notifications", description = "Send and manage email notifications")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ─── Send endpoints ────────────────────────────────────────────────────────

    @Operation(summary = "Send a notification",
               description = "Sends a templated email notification. Called internally by booking/payment modules.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Notification queued/sent"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/send")

    public ResponseEntity<NotificationDTOs.NotificationResponse> send(
            @Valid @RequestBody NotificationDTOs.SendRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.send(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/events/booking-confirmed")
   // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SYSTEM')")
    public ResponseEntity<NotificationDTOs.NotificationResponse> bookingConfirmed(
            @Valid @RequestBody NotificationDTOs.BookingConfirmedEvent event) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.sendBookingConfirmed(event));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/events/booking-cancelled")
    //@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SYSTEM')")
    public ResponseEntity<NotificationDTOs.NotificationResponse> bookingCancelled(
            @Valid @RequestBody NotificationDTOs.BookingCancelledEvent event) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.sendBookingCancelled(event));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/events/payment")
   // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SYSTEM')")
    public ResponseEntity<NotificationDTOs.NotificationResponse> payment(
            @Valid @RequestBody NotificationDTOs.PaymentEvent event) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.sendPaymentNotification(event));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/send/custom")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDTOs.NotificationResponse> sendCustom(
            @Valid @RequestBody NotificationDTOs.CustomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.sendCustom(request));
    }

    // ─── Query endpoints ───────────────────────────────────────────────────────
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get notification by ID")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    @GetMapping("/{id}")
   // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<NotificationDTOs.NotificationResponse> getById(
            @Parameter(description = "Notification ID") @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
   // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDTOs.PagedNotificationResponse> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getAll(page, size));
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get notifications by guest email")
    @GetMapping("/by-email")
  //  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<NotificationDTOs.PagedNotificationResponse> getByEmail(
            @Parameter(description = "Guest email address") @RequestParam String email,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getByEmail(email, page, size));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/by-status")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDTOs.PagedNotificationResponse> getByStatus(
            @Parameter(description = "Notification status") @RequestParam NotificationStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getByStatus(status, page, size));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/by-reference")
   // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<NotificationDTOs.NotificationResponse>> getByReference(
            @RequestParam Long referenceId,
            @RequestParam ReferenceType referenceType) {
        return ResponseEntity.ok(notificationService.getByReference(referenceId, referenceType));
    }

    // ─── Operations ────────────────────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/retry")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDTOs.NotificationResponse> retry(
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.retryManually(id));
    }

    // ─── Stats ─────────────────────────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(notificationService.getStats());
    }
}
