package com.swer313.projectstep1.waitinglist;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WaitingListResponseDTO {

    private final Long              id;
    private final Long              hotelId;
    private final Long              roomTypeId;
    private final String            roomTypeName;
    private final String            hotelName;
    private final String            guestEmail;
    private final String            guestName;
    private final LocalDate         checkIn;
    private final LocalDate         checkOut;
    private final WaitingListStatus status;
    private final LocalDateTime     createdAt;
    private final LocalDateTime     notifiedAt;

    // كم شخص قبلك في القائمة — مفيد للـ guest
    private final Integer           positionInQueue;

    public WaitingListResponseDTO(Long id, Long hotelId, Long roomTypeId, String roomTypeName,
                                  String hotelName, String guestEmail,
                                  String guestName, LocalDate checkIn,
                                  LocalDate checkOut, WaitingListStatus status,
                                  LocalDateTime createdAt, LocalDateTime notifiedAt,
                                  Integer positionInQueue) {
        this.id              = id;
        this.hotelId         = hotelId;
        this.roomTypeId      = roomTypeId;
        this.roomTypeName    = roomTypeName;
        this.hotelName       = hotelName;
        this.guestEmail      = guestEmail;
        this.guestName       = guestName;
        this.checkIn         = checkIn;
        this.checkOut        = checkOut;
        this.status          = status;
        this.createdAt       = createdAt;
        this.notifiedAt      = notifiedAt;
        this.positionInQueue = positionInQueue;
    }

    public Long              getId()              { return id; }
    public Long              getHotelId()         { return hotelId; }
    public Long              getRoomTypeId()      { return roomTypeId; }
    public String            getRoomTypeName()    { return roomTypeName; }
    public String            getHotelName()       { return hotelName; }
    public String            getGuestEmail()      { return guestEmail; }
    public String            getGuestName()       { return guestName; }
    public LocalDate         getCheckIn()         { return checkIn; }
    public LocalDate         getCheckOut()        { return checkOut; }
    public WaitingListStatus getStatus()          { return status; }
    public LocalDateTime     getCreatedAt()       { return createdAt; }
    public LocalDateTime     getNotifiedAt()      { return notifiedAt; }
    public Integer           getPositionInQueue() { return positionInQueue; }
}
