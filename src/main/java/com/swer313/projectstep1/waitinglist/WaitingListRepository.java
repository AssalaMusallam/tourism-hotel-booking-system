package com.swer313.projectstep1.waitinglist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaitingListRepository extends JpaRepository<WaitingListEntry, Long> {

    // منع التسجيل المكرر — نفس الـ guest على نفس الغرفة والفترة
    @Query("""
        SELECT COUNT(w) > 0 FROM WaitingListEntry w
        WHERE w.roomTypeId = :roomTypeId
          AND LOWER(w.guestEmail) = LOWER(:guestEmail)
          AND w.checkIn = :checkIn
          AND w.checkOut = :checkOut
          AND w.status IN ('WAITING', 'NOTIFIED')
        """)
    boolean existsActiveEntry(
            @Param("roomTypeId") Long roomTypeId,
            @Param("guestEmail") String guestEmail,
            @Param("checkIn")    LocalDate checkIn,
            @Param("checkOut")   LocalDate checkOut
    );

    // أول WAITING entry للغرفة في فترة معينة — الأقدم أولاً (FIFO)
    @Query("""
        SELECT w FROM WaitingListEntry w
        WHERE w.roomTypeId = :roomTypeId
          AND w.status = 'WAITING'
          AND w.checkIn <= :checkOut
          AND w.checkOut >= :checkIn
        ORDER BY w.createdAt ASC
        """)
    List<WaitingListEntry> findWaitingByRoomTypeAndPeriod(
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkIn")    LocalDate checkIn,
            @Param("checkOut")   LocalDate checkOut
    );

    // للـ Scheduler — entries NOTIFIED وفات عليها 24 ساعة
    @Query("""
        SELECT w FROM WaitingListEntry w
        WHERE w.status = 'NOTIFIED'
          AND w.notifiedAt <= :cutoff
        """)
    List<WaitingListEntry> findExpiredNotifications(
            @Param("cutoff") LocalDateTime cutoff
    );

    // للـ Admin count
    @Query("""
    SELECT COUNT(w) FROM WaitingListEntry w
    WHERE w.roomTypeId = :roomTypeId
      AND w.status = 'WAITING'
      AND w.checkIn <= :checkOut
      AND w.checkOut >= :checkIn
    """)
    long countWaitingByRoomTypeAndPeriod(
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkIn")    LocalDate checkIn,
            @Param("checkOut")   LocalDate checkOut
    );

    // للـ Admin list
    Page<WaitingListEntry> findByRoomTypeIdAndStatusOrderByCreatedAtAsc(
            Long roomTypeId, WaitingListStatus status, Pageable pageable
    );

    Page<WaitingListEntry> findByHotelIdOrderByCreatedAtDesc(Long hotelId, Pageable pageable);

    // للـ Scheduler — entries WAITING أو NOTIFIED وتاريخها عدى
    @Query("""
        SELECT w FROM WaitingListEntry w
        WHERE w.status IN ('WAITING', 'NOTIFIED')
          AND w.checkIn < :today
        """)
    List<WaitingListEntry> findDateExpiredEntries(
            @Param("today") LocalDate today
    );

    // guest يشوف تسجيلاته
    Page<WaitingListEntry> findByGuestEmailIgnoreCaseOrderByCreatedAtDesc(
            String guestEmail, Pageable pageable
    );

    // guest يلغي تسجيله — نجيبه بالـ id والـ email معاً للأمان
    Optional<WaitingListEntry> findByIdAndGuestEmailIgnoreCase(
            Long id, String guestEmail
    );
}
