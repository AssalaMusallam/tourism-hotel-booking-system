package com.swer313.projectstep1.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository repository;

    @Test
    void findDueForRetry_and_findStuckPending_and_countSentSince() {
        LocalDateTime now = LocalDateTime.now();

        Notification retry = Notification.builder()
                .recipientEmail("a@b.com")
                .recipientName("A")
                .type(NotificationType.CUSTOM)
                .status(NotificationStatus.RETRY_SCHEDULED)
                .nextRetryAt(now.minusMinutes(1))
                .createdAt(now.minusDays(1))
                .build();

        Notification pending = Notification.builder()
                .recipientEmail("x@y.com")
                .recipientName("X")
                .type(NotificationType.CUSTOM)
                .status(NotificationStatus.PENDING)
                .createdAt(now.minusDays(3))
                .build();

        Notification sent = Notification.builder()
                .recipientEmail("x@y.com")
                .recipientName("X")
                .type(NotificationType.CUSTOM)
                .status(NotificationStatus.SENT)
                .sentAt(now.minusHours(1))
                .createdAt(now.minusDays(1))
                .build();

        repository.saveAll(List.of(retry, pending, sent));

        List<Notification> due = repository.findDueForRetry(now);
        assertThat(due).extracting(Notification::getStatus).contains(NotificationStatus.RETRY_SCHEDULED);

        List<Notification> stuck = repository.findStuckPending(now.minusDays(1));
        assertThat(stuck).extracting(Notification::getStatus).contains(NotificationStatus.PENDING);

        long countSent = repository.countSentSince(now.minusDays(2));
        assertThat(countSent).isGreaterThanOrEqualTo(1);

        // recipient paging query
        var page = repository.findByRecipientEmailOrderByCreatedAtDesc("x@y.com", PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
    }
}

