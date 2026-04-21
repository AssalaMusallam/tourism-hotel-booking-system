package com.swer313.projectstep1.notification;

import com.swer313.projectstep1.notification.NotificationDTOs.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int   MAX_RETRY_ATTEMPTS   = 3;
    private static final int[] RETRY_DELAYS_MINUTES  = {5, 30, 120};

    private final NotificationRepository notificationRepository;
    private final JavaMailSender         mailSender;
    private final EmailTemplateBuilder   templateBuilder;
    private final NotificationMapper     mapper;

    // ─── Send methods ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public NotificationResponse send(SendRequest request) {
        log.info("Sending notification of type {} to {}",
                request.getType(), request.getRecipientEmail());

        String subject = templateBuilder.buildSubject(request.getType(), request);
        String body    = templateBuilder.buildBody(request.getType(), request);

        Notification notification = Notification.builder()
                .recipientEmail(request.getRecipientEmail())
                .recipientName(request.getRecipientName())
                .type(request.getType())
                .status(NotificationStatus.PENDING)
                .subject(subject)
                .body(body)
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .retryCount(0)
                .build();

        notification = notificationRepository.save(notification);
        attemptSend(notification, subject, body);
        return mapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public NotificationResponse sendBookingConfirmed(BookingConfirmedEvent event) {
        SendRequest req = SendRequest.builder()
                .recipientEmail(event.getGuestEmail())
                .recipientName(event.getGuestName())
                .type(NotificationType.BOOKING_CONFIRMED)
                .referenceId(event.getBookingId())
                .referenceType(ReferenceType.BOOKING)
                .hotelName(event.getHotelName())
                .roomType(event.getRoomType())
                .checkInDate(event.getCheckInDate())
                .checkOutDate(event.getCheckOutDate())
                .totalAmount(event.getTotalAmount())
                .bookingReference(event.getBookingReference())
                .build();
        return send(req);
    }

    @Override
    @Transactional
    public NotificationResponse sendBookingCancelled(BookingCancelledEvent event) {
        SendRequest req = SendRequest.builder()
                .recipientEmail(event.getGuestEmail())
                .recipientName(event.getGuestName())
                .type(NotificationType.BOOKING_CANCELLED)
                .referenceId(event.getBookingId())
                .referenceType(ReferenceType.BOOKING)
                .hotelName(event.getHotelName())
                .bookingReference(event.getBookingReference())
                .cancellationReason(event.getCancellationReason())
                .build();
        return send(req);
    }

    @Override
    @Transactional
    public NotificationResponse sendPaymentNotification(PaymentEvent event) {
        NotificationType type;
        if (event.isRefund())       type = NotificationType.PAYMENT_REFUNDED;
        else if (event.isSuccess()) type = NotificationType.PAYMENT_SUCCESS;
        else                        type = NotificationType.PAYMENT_FAILED;

        SendRequest req = SendRequest.builder()
                .recipientEmail(event.getGuestEmail())
                .recipientName(event.getGuestName())
                .type(type)
                .referenceId(event.getPaymentId())
                .referenceType(ReferenceType.PAYMENT)
                .totalAmount(event.getAmount())
                .paymentMethod(event.getPaymentMethod())
                .cancellationReason(event.getFailureReason())
                .build();
        return send(req);
    }

    @Override
    @Transactional
    public NotificationResponse sendCustom(CustomRequest request) {
        Notification notification = Notification.builder()
                .recipientEmail(request.getRecipientEmail())
                .recipientName(request.getRecipientName())
                .type(NotificationType.CUSTOM)
                .status(NotificationStatus.PENDING)
                .subject(request.getSubject())
                .body(request.getBody())
                .referenceType(ReferenceType.SYSTEM)
                .retryCount(0)
                .build();

        notification = notificationRepository.save(notification);
        attemptSend(notification, request.getSubject(), request.getBody());
        return mapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public NotificationResponse sendBookingNotification(String guestEmail,
                                                        NotificationType type,
                                                        String subject,
                                                        String body) {
        Notification notification = Notification.builder()
                .recipientEmail(guestEmail)
                .recipientName("Guest")
                .type(type)
                .status(NotificationStatus.PENDING)
                .subject(subject)
                .body(body)
                .referenceType(ReferenceType.BOOKING)
                .retryCount(0)
                .build();

        notification = notificationRepository.save(notification);
        attemptSend(notification, subject, body);
        return mapper.toResponse(notificationRepository.save(notification));
    }

    // ─── Query methods ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedNotificationResponse getByEmail(String email, int page, int size) {
        Page<Notification> p = notificationRepository
                .findByRecipientEmailOrderByCreatedAtDesc(email, PageRequest.of(page, size));
        return toPagedResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedNotificationResponse getAll(int page, int size) {
        Page<Notification> p = notificationRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return toPagedResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedNotificationResponse getByStatus(NotificationStatus status, int page, int size) {
        Page<Notification> p = notificationRepository
                .findByStatusOrderByCreatedAtDesc(status, PageRequest.of(page, size));
        return toPagedResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByReference(Long referenceId, ReferenceType type) {
        return notificationRepository
                .findByReferenceIdAndReferenceType(referenceId, type)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        return Map.of(
                "total",             notificationRepository.count(),
                "sent",              notificationRepository.countByStatus(NotificationStatus.SENT),
                "failed",            notificationRepository.countByStatus(NotificationStatus.FAILED),
                "pending",           notificationRepository.countByStatus(NotificationStatus.PENDING),
                "retryScheduled",    notificationRepository.countByStatus(NotificationStatus.RETRY_SCHEDULED),
                "permanentlyFailed", notificationRepository.countByStatus(NotificationStatus.PERMANENTLY_FAILED),
                "sentLast24h",       notificationRepository.countSentSince(LocalDateTime.now().minusHours(24))
        );
    }

    // ─── Retry ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public NotificationResponse retryManually(Long id) {
        Notification notification = findOrThrow(id);

        if (notification.getStatus() == NotificationStatus.SENT)
            throw new IllegalStateException("Notification already sent successfully.");

        if (notification.getRetryCount() >= MAX_RETRY_ATTEMPTS)
            throw new IllegalStateException(
                    "Maximum retry attempts (" + MAX_RETRY_ATTEMPTS + ") already reached.");

        attemptSend(notification, notification.getSubject(), notification.getBody());
        return mapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void processRetryQueue() {
        List<Notification> due = notificationRepository.findDueForRetry(LocalDateTime.now());
        if (!due.isEmpty())
            log.info("Retry scheduler: processing {} notification(s)", due.size());

        for (Notification n : due) {
            attemptSend(n, n.getSubject(), n.getBody());
            notificationRepository.save(n);
        }
    }

    // ─── Core send logic ───────────────────────────────────────────────────────

    private void attemptSend(Notification notification, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(notification.getRecipientEmail());
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setErrorMessage(null);
            log.info("Notification {} sent to {}",
                    notification.getId(), notification.getRecipientEmail());

        } catch (MessagingException | org.springframework.mail.MailException ex) {
            int attempts = notification.getRetryCount() + 1;
            notification.setRetryCount(attempts);
            notification.setErrorMessage(ex.getMessage());

            if (attempts >= MAX_RETRY_ATTEMPTS) {
                notification.setStatus(NotificationStatus.PERMANENTLY_FAILED);
                log.error("Notification {} permanently failed after {} attempts: {}",
                        notification.getId(), attempts, ex.getMessage());
            } else {
                notification.setStatus(NotificationStatus.RETRY_SCHEDULED);
                notification.setNextRetryAt(
                        LocalDateTime.now().plusMinutes(RETRY_DELAYS_MINUTES[attempts - 1]));
                log.warn("Notification {} failed (attempt {}), retry at {}",
                        notification.getId(), attempts, notification.getNextRetryAt());
            }
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private Notification findOrThrow(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    private PagedNotificationResponse toPagedResponse(Page<Notification> page) {
        return PagedNotificationResponse.builder()
                .content(page.getContent().stream().map(mapper::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}