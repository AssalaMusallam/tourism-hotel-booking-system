package com.swer313.projectstep1.booking;

import com.swer313.projectstep1.errors.BaseApiException;
import org.springframework.http.HttpStatus;

class GuestCapacityExceededException extends BaseApiException {

    public GuestCapacityExceededException(int requested, int max, String roomName) {
        super(
                HttpStatus.BAD_REQUEST,
                String.format(
                        "Requested %d guests exceeds capacity of %d for room '%s'",
                        requested, max, roomName
                )
        );
    }
}