package com.swer313.projectstep1.booking;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Request DTO for creating a new booking.
 * Bean validation handles null/format checks.
 * Business rules (capacity, overlap, date logic) are validated in the service.
 */
public class BookingRequestDTO {

    // ── Room ──────────────────────────────────────────────────────────────────
    @NotNull(message = "roomTypeId is required")
    private Long roomTypeId;

    // ── Guest info ─────────────────────────────────────────────────────────────
    @NotBlank(message = "guestName is required")
    @Size(min = 2, max = 150, message = "guestName must be 2–150 characters")
    private String guestName;

    @NotBlank(message = "guestEmail is required")
    @Email(message = "guestEmail must be a valid email address")
    private String guestEmail;

    @NotBlank(message = "guestPhone is required")
    @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "guestPhone format is invalid")
    private String guestPhone;

    // ── Guests breakdown ───────────────────────────────────────────────────────
    @Min(value = 1, message = "adults must be at least 1")
    private int adults = 1;

    @Min(value = 0, message = "children must be 0 or more")
    private int children = 0;

    // ── Dates ─────────────────────────────────────────────────────────────────
    @NotNull(message = "checkIn is required")
    @FutureOrPresent(message = "checkIn must be today or a future date")
    private LocalDate checkIn;

    @NotNull(message = "checkOut is required")
    @Future(message = "checkOut must be a future date")
    private LocalDate checkOut;

    // ── Optional ──────────────────────────────────────────────────────────────
    @Size(max = 1000, message = "guestNotes cannot exceed 1000 characters")
    private String guestNotes;

    // ===== Getters / Setters =====
    public Long getRoomTypeId()                     { return roomTypeId; }
    public void setRoomTypeId(Long id)              { this.roomTypeId = id; }

    public String getGuestName()                    { return guestName; }
    public void setGuestName(String guestName)      { this.guestName = guestName; }

    public String getGuestEmail()                   { return guestEmail; }
    public void setGuestEmail(String guestEmail)    { this.guestEmail = guestEmail; }

    public String getGuestPhone()                   { return guestPhone; }
    public void setGuestPhone(String guestPhone)    { this.guestPhone = guestPhone; }

    public int getAdults()                          { return adults; }
    public void setAdults(int adults)               { this.adults = adults; }

    public int getChildren()                        { return children; }
    public void setChildren(int children)           { this.children = children; }

    public LocalDate getCheckIn()                   { return checkIn; }
    public void setCheckIn(LocalDate checkIn)       { this.checkIn = checkIn; }

    public LocalDate getCheckOut()                  { return checkOut; }
    public void setCheckOut(LocalDate checkOut)     { this.checkOut = checkOut; }

    public String getGuestNotes()                   { return guestNotes; }
    public void setGuestNotes(String guestNotes)    { this.guestNotes = guestNotes; }
}