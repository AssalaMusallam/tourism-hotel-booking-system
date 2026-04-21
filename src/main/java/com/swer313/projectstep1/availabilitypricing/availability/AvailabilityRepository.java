package com.swer313.projectstep1.availabilitypricing.availability;

import com.swer313.projectstep1.booking.Booking;
import com.swer313.projectstep1.booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface AvailabilityRepository
        extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    /**
     * count للـ availability check الفردي.
     * يُستخدم في checkAvailability() و remainingUnits().
     */
    long countByRoomType_IdAndStatusInAndCheckInLessThanAndCheckOutGreaterThan(
            Long roomTypeId,
            Collection<BookingStatus> statuses,
            LocalDate checkOut,
            LocalDate checkIn
    );

    /**
     * Batch count — يحل مشكلة N+1 في checkHotelAvailability().
     * يجيب عدد الحجوزات النشطة لكل roomType بـ query واحدة.
     */
    @Query("""
        SELECT b.roomType.id AS roomTypeId,
               COUNT(b)      AS bookedCount
        FROM   Booking b
        WHERE  b.roomType.id IN :roomTypeIds
          AND  b.status       IN :statuses
          AND  b.checkIn       < :checkOut
          AND  b.checkOut      > :checkIn
        GROUP BY b.roomType.id
    """)
    List<BookedCountProjection> countBookedByRoomTypeIds(
            @Param("roomTypeIds") List<Long> roomTypeIds,
            @Param("statuses")    List<BookingStatus> statuses,
            @Param("checkIn")     LocalDate checkIn,
            @Param("checkOut")    LocalDate checkOut
    );
}