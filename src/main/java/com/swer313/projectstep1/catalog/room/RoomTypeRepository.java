package com.swer313.projectstep1.catalog.room;
import com.swer313.projectstep1.catalog.hotel.Hotel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long>, JpaSpecificationExecutor<RoomType> {

    // ✅ التحقق من تكرار الاسم داخل نفس الفندق
    boolean existsByHotel_IdAndNameIgnoreCase(Long hotelId, String name);

    boolean existsByNameAndHotel(String name, Hotel hotel);

    // ✅ نفس الشيء لكن باستثناء الـ id الحالي (للـ update)
    boolean existsByHotel_IdAndNameIgnoreCaseAndIdNot(Long hotelId, String name, Long id);



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT rt FROM RoomType rt WHERE rt.id = :id")
    Optional<RoomType> findByIdWithLock(@Param("id") Long id);
    /**
     * مجموع الغرف الفعلية للفندق.
     *
     * ليش SUM(totalUnits) مش COUNT؟
     * COUNT → يحسب عدد أنواع الغرف (مثلاً 3 أنواع)
     * SUM   → يحسب الغرف الفعلية (مثلاً Standard=20 + Deluxe=10 + Suite=5 = 35)
     *
     * فقط الغرف ACTIVE تُحسب — الـ INACTIVE مش متاحة للحجز.
     * COALESCE(..., 0) يمنع null لو الفندق ما عنده غرف.
     */
    @Query("""
    SELECT COALESCE(SUM(rt.totalUnits), 0) FROM RoomType rt
    WHERE rt.hotel.id = :hotelId
      AND rt.status = com.swer313.projectstep1.catalog.room.RoomTypeStatus.ACTIVE
    """)
    Integer sumTotalUnitsByHotelId(@Param("hotelId") Long hotelId);
}


