package com.swer313.projectstep1.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ─── Guest-facing queries ─────────────────────────────────────────────────

    Page<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email, Pageable pageable);

    Page<Notification> findByRecipientEmailAndTypeOrderByCreatedAtDesc(
            String email, NotificationType type, Pageable pageable);

    List<Notification> findByReferenceIdAndReferenceType(Long referenceId, ReferenceType referenceType);

    // ─── Admin / manager queries ──────────────────────────────────────────────

    Page<Notification> findByStatusOrderByCreatedAtDesc(NotificationStatus status, Pageable pageable);

    Page<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type, Pageable pageable);

    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // ─── Retry scheduler queries ──────────────────────────────────────────────

    @Query("SELECT n FROM Notification n WHERE n.status = 'RETRY_SCHEDULED' AND n.nextRetryAt <= :now")
    List<Notification> findDueForRetry(@Param("now") LocalDateTime now);

    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.createdAt <= :cutoff")
    List<Notification> findStuckPending(@Param("cutoff") LocalDateTime cutoff);

    // ─── Statistics ───────────────────────────────────────────────────────────

    long countByStatus(NotificationStatus status);

    long countByType(NotificationType type);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = 'SENT' AND n.sentAt >= :from")
    long countSentSince(@Param("from") LocalDateTime from);
}
