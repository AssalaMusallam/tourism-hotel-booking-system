package com.swer313.projectstep1.booking;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
@Repository
public interface BookingRepository
        extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    // ── Guest queries ─────────────────────────────────────────────────────────
    boolean existsByRoomTypeIdAndStatusIn(Long roomTypeId, List<BookingStatus> statuses);
    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.roomType rt
            JOIN FETCH rt.hotel
            WHERE LOWER(b.guestEmail) = LOWER(:email)
            """)
    Page<Booking> findByGuestEmailIgnoreCase(
            @Param("email") String guestEmail, Pageable pageable);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.roomType rt
            JOIN FETCH rt.hotel
            WHERE LOWER(b.guestEmail) = LOWER(:email)
              AND b.status = :status
            """)
    Page<Booking> findByGuestEmailIgnoreCaseAndStatus(
            @Param("email") String guestEmail,
            @Param("status") BookingStatus status,
            Pageable pageable);

    // ── Manager queries ───────────────────────────────────────────────────────

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.roomType rt
            JOIN FETCH rt.hotel h
            WHERE h.id = :hotelId
            """)
    Page<Booking> findByRoomType_Hotel_Id(
            @Param("hotelId") Long hotelId, Pageable pageable);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.roomType rt
            JOIN FETCH rt.hotel h
            WHERE h.id = :hotelId
              AND b.status = :status
            """)
    Page<Booking> findByRoomType_Hotel_IdAndStatus(
            @Param("hotelId") Long hotelId,
            @Param("status") BookingStatus status,
            Pageable pageable);

    // ── Scheduler ─────────────────────────────────────────────────────────────

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.roomType rt
            JOIN FETCH rt.hotel
            WHERE b.status = :status
              AND b.checkIn = :checkInDate
            """)
    List<Booking> findConfirmedWithRoomTypeAndHotel(
            @Param("status") BookingStatus status,
            @Param("checkInDate") LocalDate checkInDate);

    // ── Admin Reports ─────────────────────────────────────────────────────────

    /**
     * مجموع الإيرادات لفندق في فترة معينة.
     * بتجيب فقط الحجوزات CONFIRMED — المؤكدة فعلاً.
     * SUM ترجع null لو ما في حجوزات — الـ DTO بيتعامل معها.
     */
    @Query("""

            SELECT SUM(b.totalPrice) FROM Booking b
    WHERE b.roomType.hotel.id = :hotelId
      AND b.status = com.swer313.projectstep1.booking.BookingStatus.CONFIRMED
      AND b.checkIn >= :from
      AND b.checkIn <= :to
    """)
    BigDecimal sumRevenueByHotelAndDateRange(
            @Param("hotelId") Long hotelId,
            @Param("from")    LocalDate from,
            @Param("to")      LocalDate to
    );

    /**
     * عدد الحجوزات المؤكدة لفندق في فترة معينة.
     */
    @Query("""
    SELECT COUNT(b) FROM Booking b
    WHERE b.roomType.hotel.id = :hotelId
      AND b.status = com.swer313.projectstep1.booking.BookingStatus.CONFIRMED
      AND b.checkIn >= :from
      AND b.checkIn <= :to
    """)
    Long countConfirmedByHotelAndDateRange(
            @Param("hotelId") Long hotelId,
            @Param("from")    LocalDate from,
            @Param("to")      LocalDate to
    );

    /**
     * مجموع الأيام المحجوزة مع حساب التداخل الصحيح.
     * <p>
     * المشكلة مع الـ query البسيطة:
     * حجز: checkIn=28-Feb, checkOut=03-Mar
     * → بالـ query البسيطة: ما يُحسب لأنه ما بدأ داخل مارس ❌
     * → بالـ query الصحيحة: يُحسب 3 أيام داخل مارس ✅
     * <p>
     * الحل:
     * actualStart = MAX(checkIn, startOfMonth)
     * actualEnd   = MIN(checkOut, endExclusive)
     * days = DATEDIFF(actualEnd, actualStart)
     * <p>
     * endExclusive = أول يوم الشهر التالي (مش آخر يوم الشهر الحالي)
     * عشان نضمن دقة الحساب لآخر يوم في الشهر.
     * <p>
     * COALESCE(..., 0) يمنع null لو ما في حجوزات.
     */
    @Query("""
    SELECT COALESCE(SUM(
        FUNCTION('DATEDIFF',
            LEAST(b.checkOut, :endExclusive),
            GREATEST(b.checkIn, :start)
        )
    ), 0)
    FROM Booking b
    WHERE b.roomType.hotel.id = :hotelId
      AND b.status = com.swer313.projectstep1.booking.BookingStatus.CONFIRMED
      AND b.checkOut > :start
      AND b.checkIn < :endExclusive
    """)
    Long sumBookedDaysByHotelAndMonth(
            @Param("hotelId")      Long      hotelId,
            @Param("start")        LocalDate start,
            @Param("endExclusive") LocalDate endExclusive
    );

    /**
     * أشهر أنواع الغرف مرتبة تنازلياً.
     * new PopularRoomDto(...) — الـ JPQL يبني الـ DTO مباشرة بدون mapper.
     * GROUP BY b.roomType.name — يجمع الحجوزات حسب نوع الغرفة.
     * ORDER BY COUNT(b) DESC — الأكثر حجزاً أولاً.
     */
    @Query("""
    SELECT new com.swer313.projectstep1.admin.PopularRoomDto(
        b.roomType.name, COUNT(b)
    )
    FROM Booking b
    WHERE b.roomType.hotel.id = :hotelId
      AND b.status = com.swer313.projectstep1.booking.BookingStatus.CONFIRMED
    GROUP BY b.roomType.name
    ORDER BY COUNT(b) DESC
    """)
    List<com.swer313.projectstep1.admin.PopularRoomDto> findPopularRoomTypesByHotel(
            @Param("hotelId") Long hotelId
    );
}