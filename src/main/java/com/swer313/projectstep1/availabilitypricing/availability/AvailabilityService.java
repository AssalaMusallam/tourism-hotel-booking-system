package com.swer313.projectstep1.availabilitypricing.availability;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AvailabilityService {

    /**
     * فحص availability لـ roomType معين مع السعر الكامل.
     */
    AvailabilityResponseDto checkAvailability(
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guests
    );

    /**
     * فحص availability لكل roomTypes في فندق مع pagination.
     * availableOnly=true → فلترة في DB مو in-memory.
     */
    PagedResponse<AvailabilitySummaryDto> checkHotelAvailability(
            Long hotelId,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guests,
            String q,
            Boolean availableOnly,
            Pageable pageable
    );
}