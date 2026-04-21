package com.swer313.projectstep1.availabilitypricing.availability;

import com.swer313.projectstep1.availabilitypricing.pricing.PriceBreakdownDTO;
import com.swer313.projectstep1.availabilitypricing.pricing.PricingCalculator;
import com.swer313.projectstep1.booking.BookingStatus;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeNotFoundException;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import com.swer313.projectstep1.catalog.room.RoomTypeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final List<BookingStatus> ACTIVE_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final AvailabilityRepository availabilityRepository;
    private final RoomTypeRepository     roomTypeRepository;
    private final PricingCalculator      pricingCalculator;

    public AvailabilityServiceImpl(
            AvailabilityRepository availabilityRepository,
            RoomTypeRepository     roomTypeRepository,
            PricingCalculator      pricingCalculator) {

        this.availabilityRepository = availabilityRepository;
        this.roomTypeRepository     = roomTypeRepository;
        this.pricingCalculator      = pricingCalculator;
    }

    // ── checkAvailability ─────────────────────────────────────────────────────

    @Override
    public AvailabilityResponseDto checkAvailability(
            Long roomTypeId, LocalDate checkIn,
            LocalDate checkOut, Integer guests) {

        validateDates(checkIn, checkOut);

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RoomTypeNotFoundException(roomTypeId));

        if (roomType.getStatus() != RoomTypeStatus.ACTIVE) {
            throw new AvailabilityRoomTypeInactiveException(roomTypeId);
        }

        if (guests != null && guests > roomType.getCapacity()) {
            throw new AvailabilityGuestsExceedCapacityException(
                    guests, roomType.getCapacity());
        }

        long bookedUnits    = countBooked(roomTypeId, checkIn, checkOut);
        long remainingUnits = Math.max(0L, roomType.getTotalUnits() - bookedUnits);
        long nights         = ChronoUnit.DAYS.between(checkIn, checkOut);

        // السعر الكامل مع تفاصيل كل ليلة
        PriceBreakdownDTO pricing = pricingCalculator
                .calculateBreakdown(roomType.getBasePrice(), checkIn, checkOut);

        return new AvailabilityResponseDto(
                roomType.getHotelId(),
                roomType.getHotel().getName(),
                roomType.getId(),
                roomType.getName(),
                checkIn, checkOut, nights,
                guests,
                roomType.getCapacity(),
                roomType.getTotalUnits(),
                bookedUnits,
                remainingUnits,
                remainingUnits > 0,
                roomType.getBasePrice(),
                pricing
        );
    }

    // ── checkHotelAvailability ────────────────────────────────────────────────

    @Override
    public PagedResponse<AvailabilitySummaryDto> checkHotelAvailability(
            Long hotelId, LocalDate checkIn, LocalDate checkOut,
            Integer guests, String q, Boolean availableOnly, Pageable pageable) {

        validateDates(checkIn, checkOut);

        // availableOnly filter داخل الـ DB (subquery) — مو in-memory
        Specification<RoomType> spec = AvailabilitySpecifications.hotelAvailabilityFilter(
                hotelId, guests, q, availableOnly, checkIn, checkOut
        );

        Page<RoomType> page = roomTypeRepository.findAll(spec, pageable);

        if (page.isEmpty()) {
            return PagedResponse.from(page, List.of());
        }

        // Batch query واحدة بدل N queries — حل مشكلة N+1
        List<Long> roomTypeIds = page.getContent()
                .stream()
                .map(RoomType::getId)
                .toList();

        Map<Long, Long> bookedCountMap = availabilityRepository
                .countBookedByRoomTypeIds(roomTypeIds, ACTIVE_STATUSES, checkIn, checkOut)
                .stream()
                .collect(Collectors.toMap(
                        BookedCountProjection::getRoomTypeId,
                        BookedCountProjection::getBookedCount
                ));

        List<AvailabilitySummaryDto> content = page.getContent().stream()
                .map(rt -> {
                    long booked    = bookedCountMap.getOrDefault(rt.getId(), 0L);
                    long remaining = Math.max(0L, rt.getTotalUnits() - booked);
                    return new AvailabilitySummaryDto(
                            rt.getHotelId(),
                            rt.getHotel().getName(),
                            rt.getId(),
                            rt.getName(),
                            rt.getCapacity(),
                            rt.getTotalUnits(),
                            booked,
                            remaining,
                            remaining > 0,
                            rt.getStatus(),
                            rt.getBasePrice()
                    );
                })
                .toList();

        return PagedResponse.from(page, content);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private long countBooked(Long roomTypeId,
                             LocalDate checkIn, LocalDate checkOut) {
        return availabilityRepository
                .countByRoomType_IdAndStatusInAndCheckInLessThanAndCheckOutGreaterThan(
                        roomTypeId, ACTIVE_STATUSES, checkOut, checkIn
                );
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null) {
            throw new AvailabilityDateRangeException("checkIn is required");
        }
        if (checkOut == null) {
            throw new AvailabilityDateRangeException("checkOut is required");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new AvailabilityDateRangeException(
                    "checkOut must be strictly after checkIn");
        }
    }
}