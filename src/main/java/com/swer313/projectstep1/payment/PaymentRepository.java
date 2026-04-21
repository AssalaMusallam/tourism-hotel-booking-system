package com.swer313.projectstep1.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository
        extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    /** آخر payment لحجز معيّن (الأحدث) */
    Optional<Payment> findTopByBookingIdOrderByCreatedAtDesc(Long bookingId);

    /** كل محاولات الدفع لحجز معيّن مرتبة من الأحدث */
    List<Payment> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

    /** هل يوجد payment نشط (PENDING أو SUCCESS) لهذا الحجز؟ */
    boolean existsByBookingIdAndStatusIn(Long bookingId, Collection<PaymentStatus> statuses);

    /** جلب بـ transactionReference */
    Optional<Payment> findByTransactionReference(String transactionReference);

    /** إحصائيات: عدد الـ payments لكل status */
    @Query("SELECT p.status, COUNT(p) FROM Payment p GROUP BY p.status")
    List<Object[]> countGroupedByStatus();

    /** إجمالي المبالغ المدفوعة (SUCCESS فقط) */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS'")
    java.math.BigDecimal sumSuccessfulPayments();
}