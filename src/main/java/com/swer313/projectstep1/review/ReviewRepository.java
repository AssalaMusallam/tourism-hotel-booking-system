package com.swer313.projectstep1.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // منع التكرار — التحقق قبل الحفظ (الـ unique constraint بالداتابيس هو الضمان الحقيقي)
    boolean existsByBooking_Id(Long bookingId);

    // reviews فندق مرتبة من الأحدث
    Page<Review> findByHotelIdOrderByCreatedAtDesc(Long hotelId, Pageable pageable);

    // aggregate query — كل البيانات اللازمة للـ summary برحلة واحدة للداتابيس
    @Query("""
        SELECT
            AVG(r.rating),
            COUNT(r),
            SUM(CASE WHEN r.rating = 5 THEN 1 ELSE 0 END),
            SUM(CASE WHEN r.rating = 4 THEN 1 ELSE 0 END),
            SUM(CASE WHEN r.rating = 3 THEN 1 ELSE 0 END),
            SUM(CASE WHEN r.rating = 2 THEN 1 ELSE 0 END),
            SUM(CASE WHEN r.rating = 1 THEN 1 ELSE 0 END)
        FROM Review r
        WHERE r.hotelId = :hotelId
        """)
    Object[] getRatingSummaryByHotelId(@Param("hotelId") Long hotelId);

    // للـ Scheduler — حجوزات مكتملة ما كتبت review وفات عليها يومين بعد الـ checkout
    @Query("""
        SELECT b FROM com.swer313.projectstep1.booking.Booking b
        JOIN FETCH b.roomType rt
        JOIN FETCH rt.hotel
        WHERE b.status = com.swer313.projectstep1.booking.BookingStatus.COMPLETED
          AND b.checkOut = :targetDate
          AND NOT EXISTS (
              SELECT r FROM Review r WHERE r.booking.id = b.id
          )
        """)
    List<com.swer313.projectstep1.booking.Booking> findCompletedBookingsWithoutReview(
            @Param("targetDate") LocalDate targetDate
    );
}