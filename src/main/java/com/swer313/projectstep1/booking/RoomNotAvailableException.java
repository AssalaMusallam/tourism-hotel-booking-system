package com.swer313.projectstep1.booking;

import com.swer313.projectstep1.errors.BaseApiException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

class RoomNotAvailableException extends BaseApiException {

    public RoomNotAvailableException(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        super(
                HttpStatus.CONFLICT,
                String.format(
                        "Room type %d is not available from %s to %s",
                        roomTypeId, checkIn, checkOut
                )
        );
    }
}