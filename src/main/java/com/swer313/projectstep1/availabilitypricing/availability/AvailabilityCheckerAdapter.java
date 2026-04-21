package com.swer313.projectstep1.availabilitypricing.availability;
import com.swer313.projectstep1.booking.AvailabilityChecker;
import com.swer313.projectstep1.booking.BookingStatus;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

/**
 * Adapter يربط الـ booking module بالـ availability module.
 *
 * الـ lock مهم هنا:
 * - findByIdWithLock() يعمل SELECT ... FOR UPDATE على الـ RoomType row
 * - يمنع thread ثاني يقرأ نفس الـ count حتى تنتهي الـ transaction
 * - هاد هو الحل الوحيد الصحيح لمنع double-booking
 */
@Component
public class AvailabilityCheckerAdapter implements AvailabilityChecker {

    private static final List<BookingStatus> ACTIVE_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final AvailabilityRepository availabilityRepository;
    private final RoomTypeRepository     roomTypeRepository;

    public AvailabilityCheckerAdapter(
            AvailabilityRepository availabilityRepository,
            RoomTypeRepository roomTypeRepository) {
        this.availabilityRepository = availabilityRepository;
        this.roomTypeRepository     = roomTypeRepository;
    }

    @Override
    @Transactional
    public boolean isAvailable(Long roomTypeId,
                               LocalDate checkIn, LocalDate checkOut) {
        return remainingUnits(roomTypeId, checkIn, checkOut) > 0;
    }

    /**
     * @Transactional ضروري — الـ PESSIMISTIC_WRITE lock
     * لازم يكون ضمن transaction نشطة عشان يشتغل.
     */
    @Override
    @Transactional
    public long remainingUnits(Long roomTypeId,
                               LocalDate checkIn, LocalDate checkOut) {
        return roomTypeRepository.findByIdWithLock(roomTypeId)
                .map(rt -> {
                    long booked = availabilityRepository
                            .countByRoomType_IdAndStatusInAndCheckInLessThanAndCheckOutGreaterThan(
                                    roomTypeId, ACTIVE_STATUSES, checkOut, checkIn
                            );
                    return Math.max(0L, rt.getTotalUnits() - booked);
                })
                .orElse(0L);
    }
}