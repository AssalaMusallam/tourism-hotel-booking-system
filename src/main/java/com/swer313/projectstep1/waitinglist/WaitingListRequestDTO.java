package com.swer313.projectstep1.waitinglist;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class WaitingListRequestDTO {

    @NotNull(message = "roomTypeId is required")
    private Long roomTypeId;

    @Email(message = "guestEmail must be valid")
    private String guestEmail;

    @Size(min = 2, max = 150)
    private String guestName;

    @NotNull(message = "checkIn is required")
    @FutureOrPresent(message = "checkIn must be today or future")
    private LocalDate checkIn;

    @NotNull(message = "checkOut is required")
    @Future(message = "checkOut must be in the future")
    private LocalDate checkOut;

    public Long getRoomTypeId()              { return roomTypeId; }
    public void setRoomTypeId(Long id)       { this.roomTypeId = id; }
    public String getGuestEmail()            { return guestEmail; }
    public void setGuestEmail(String email)  { this.guestEmail = email; }
    public String getGuestName()             { return guestName; }
    public void setGuestName(String name)    { this.guestName = name; }
    public LocalDate getCheckIn()            { return checkIn; }
    public void setCheckIn(LocalDate d)      { this.checkIn = d; }
    public LocalDate getCheckOut()           { return checkOut; }
    public void setCheckOut(LocalDate d)     { this.checkOut = d; }
}
