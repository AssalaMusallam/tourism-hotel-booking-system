package com.swer313.projectstep1.booking;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponseDTO {

    private final Long          id;
    private final Long          roomTypeId;
    private final String        roomTypeName;
    private final String        hotelName;

    // ── Guest ──────────────────────────────────────────────────────────────
    private final String        guestName;
    private final String        guestEmail;
    private final String        guestPhone;
    private final int           adults;
    private final int           children;
    private final int           totalGuests;

    // ── Dates ──────────────────────────────────────────────────────────────
    private final LocalDate     checkIn;
    private final LocalDate     checkOut;
    private final long          nights;

    // ── Pricing ────────────────────────────────────────────────────────────
    private final BigDecimal    pricePerNight;
    private final BigDecimal    totalPrice;

    // ── Status ─────────────────────────────────────────────────────────────
    private final BookingStatus status;

    // ── Cancellation (null unless CANCELLED) ───────────────────────────────
    private final LocalDateTime cancelledAt;
    private final String        cancellationReason;
    private final BigDecimal    refundAmount;

    // ── Extras ─────────────────────────────────────────────────────────────
    private final String        guestNotes;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // FIX:  private
    // FIX: @JsonInclude(NON_DEFAULT)
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private final long          remainingUnits;

    public BookingResponseDTO(
            Long id, Long roomTypeId, String roomTypeName, String hotelName,
            String guestName, String guestEmail, String guestPhone,
            int adults, int children, int totalGuests,
            LocalDate checkIn, LocalDate checkOut, long nights,
            BigDecimal pricePerNight, BigDecimal totalPrice,
            BookingStatus status,
            LocalDateTime cancelledAt, String cancellationReason, BigDecimal refundAmount,
            String guestNotes,
            LocalDateTime createdAt, LocalDateTime updatedAt, long remainingUnits
    ) {
        this.id                 = id;
        this.roomTypeId         = roomTypeId;
        this.roomTypeName       = roomTypeName;
        this.hotelName          = hotelName;
        this.guestName          = guestName;
        this.guestEmail         = guestEmail;
        this.guestPhone         = guestPhone;
        this.adults             = adults;
        this.children           = children;
        this.totalGuests        = totalGuests;
        this.checkIn            = checkIn;
        this.checkOut           = checkOut;
        this.nights             = nights;
        this.pricePerNight      = pricePerNight;
        this.totalPrice         = totalPrice;
        this.status             = status;
        this.cancelledAt        = cancelledAt;
        this.cancellationReason = cancellationReason;
        this.refundAmount       = refundAmount;
        this.guestNotes         = guestNotes;
        this.createdAt          = createdAt;
        this.updatedAt          = updatedAt;
        this.remainingUnits     = remainingUnits;
    }

    public Long getId()                         { return id; }
    public Long getRoomTypeId()                 { return roomTypeId; }
    public String getRoomTypeName()             { return roomTypeName; }
    public String getHotelName()                { return hotelName; }
    public String getGuestName()                { return guestName; }
    public String getGuestEmail()               { return guestEmail; }
    public String getGuestPhone()               { return guestPhone; }
    public int getAdults()                      { return adults; }
    public int getChildren()                    { return children; }
    public int getTotalGuests()                 { return totalGuests; }
    public LocalDate getCheckIn()               { return checkIn; }
    public LocalDate getCheckOut()              { return checkOut; }
    public long getNights()                     { return nights; }
    public BigDecimal getPricePerNight()        { return pricePerNight; }
    public BigDecimal getTotalPrice()           { return totalPrice; }
    public BookingStatus getStatus()            { return status; }
    public LocalDateTime getCancelledAt()       { return cancelledAt; }
    public String getCancellationReason()       { return cancellationReason; }
    public BigDecimal getRefundAmount()         { return refundAmount; }
    public String getGuestNotes()               { return guestNotes; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
    public long getRemainingUnits()             { return remainingUnits; }
}