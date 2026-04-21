package com.swer313.projectstep1.availabilitypricing.availability;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.swer313.projectstep1.catalog.room.RoomTypeStatus;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailabilitySummaryDto {

    private final Long           hotelId;
    private final String         hotelName;
    private final Long           roomTypeId;
    private final String         roomTypeName;
    private final int            capacity;
    private final int            totalUnits;
    private final long           bookedUnits;
    private final long           remainingUnits;
    private final boolean        available;
    private final RoomTypeStatus status;
    private final BigDecimal     basePrice;   // ← جديد: مفيد للعميل يشوف السعر في الـ list

    public AvailabilitySummaryDto(
            Long hotelId, String hotelName,
            Long roomTypeId, String roomTypeName,
            int capacity, int totalUnits,
            long bookedUnits, long remainingUnits,
            boolean available, RoomTypeStatus status,
            BigDecimal basePrice) {

        this.hotelId       = hotelId;
        this.hotelName     = hotelName;
        this.roomTypeId    = roomTypeId;
        this.roomTypeName  = roomTypeName;
        this.capacity      = capacity;
        this.totalUnits    = totalUnits;
        this.bookedUnits   = bookedUnits;
        this.remainingUnits = remainingUnits;
        this.available     = available;
        this.status        = status;
        this.basePrice     = basePrice;
    }

    public Long           getHotelId()        { return hotelId; }
    public String         getHotelName()      { return hotelName; }
    public Long           getRoomTypeId()     { return roomTypeId; }
    public String         getRoomTypeName()   { return roomTypeName; }
    public int            getCapacity()       { return capacity; }
    public int            getTotalUnits()     { return totalUnits; }
    public long           getBookedUnits()    { return bookedUnits; }
    public long           getRemainingUnits() { return remainingUnits; }
    public boolean        isAvailable()       { return available; }
    public RoomTypeStatus getStatus()         { return status; }
    public BigDecimal     getBasePrice()      { return basePrice; }
}