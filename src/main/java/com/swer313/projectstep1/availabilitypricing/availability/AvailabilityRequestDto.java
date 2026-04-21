package com.swer313.projectstep1.availabilitypricing.availability;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class AvailabilityRequestDto {

    @NotNull(message = "roomTypeId is required")
    private Long roomTypeId;

    @NotNull(message = "checkIn is required")
    @FutureOrPresent(message = "checkIn must be today or a future date")
    private LocalDate checkIn;

    @NotNull(message = "checkOut is required")
    private LocalDate checkOut;

    @Min(value = 1, message = "guests must be >= 1")
    private Integer guests;

    // ── Cross-field validation ────────────────────────────────────────────────

    @JsonIgnore
    public boolean isCheckOutAfterCheckIn() {
        if (checkIn == null || checkOut == null) return true;
        return checkOut.isAfter(checkIn);
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long getRoomTypeId()               { return roomTypeId; }
    public void setRoomTypeId(Long id)        { this.roomTypeId = id; }

    public LocalDate getCheckIn()             { return checkIn; }
    public void setCheckIn(LocalDate d)       { this.checkIn = d; }

    public LocalDate getCheckOut()            { return checkOut; }
    public void setCheckOut(LocalDate d)      { this.checkOut = d; }

    public Integer getGuests()                { return guests; }
    public void setGuests(Integer guests)     { this.guests = guests; }
}