package com.swer313.projectstep1.availabilitypricing.availability;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.swer313.projectstep1.availabilitypricing.pricing.PriceBreakdownDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailabilityResponseDto {

    private final Long             hotelId;
    private final String           hotelName;
    private final Long             roomTypeId;
    private final String           roomTypeName;
    private final LocalDate        checkIn;
    private final LocalDate        checkOut;
    private final long             nights;
    private final Integer          requestedGuests;
    private final int              capacity;
    private final int              totalUnits;
    private final long             bookedUnits;
    private final long             remainingUnits;
    private final boolean          available;
    private final BigDecimal       basePrice;       // سعر الليلة الأساسي
    private final PriceBreakdownDTO priceBreakdown; // السعر الكامل مع التفاصيل

    public AvailabilityResponseDto(
            Long hotelId, String hotelName,
            Long roomTypeId, String roomTypeName,
            LocalDate checkIn, LocalDate checkOut, long nights,
            Integer requestedGuests,
            int capacity, int totalUnits,
            long bookedUnits, long remainingUnits, boolean available,
            BigDecimal basePrice, PriceBreakdownDTO priceBreakdown) {

        this.hotelId         = hotelId;
        this.hotelName       = hotelName;
        this.roomTypeId      = roomTypeId;
        this.roomTypeName    = roomTypeName;
        this.checkIn         = checkIn;
        this.checkOut        = checkOut;
        this.nights          = nights;
        this.requestedGuests = requestedGuests;
        this.capacity        = capacity;
        this.totalUnits      = totalUnits;
        this.bookedUnits     = bookedUnits;
        this.remainingUnits  = remainingUnits;
        this.available       = available;
        this.basePrice       = basePrice;
        this.priceBreakdown  = priceBreakdown;
    }

    public Long             getHotelId()         { return hotelId; }
    public String           getHotelName()        { return hotelName; }
    public Long             getRoomTypeId()       { return roomTypeId; }
    public String           getRoomTypeName()     { return roomTypeName; }
    public LocalDate        getCheckIn()          { return checkIn; }
    public LocalDate        getCheckOut()         { return checkOut; }
    public long             getNights()           { return nights; }
    public Integer          getRequestedGuests()  { return requestedGuests; }
    public int              getCapacity()         { return capacity; }
    public int              getTotalUnits()       { return totalUnits; }
    public long             getBookedUnits()      { return bookedUnits; }
    public long             getRemainingUnits()   { return remainingUnits; }
    public boolean          isAvailable()         { return available; }
    public BigDecimal       getBasePrice()        { return basePrice; }
    public PriceBreakdownDTO getPriceBreakdown()  { return priceBreakdown; }
}