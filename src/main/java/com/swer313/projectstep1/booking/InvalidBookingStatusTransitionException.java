package com.swer313.projectstep1.booking;

import com.swer313.projectstep1.errors.BaseApiException;
import org.springframework.http.HttpStatus;

class InvalidBookingStatusTransitionException extends BaseApiException {

    public InvalidBookingStatusTransitionException(Long id, BookingStatus from, BookingStatus to) {
        super(
                HttpStatus.CONFLICT,
                String.format("Cannot transition booking %d from %s to %s", id, from, to)
        );
    }
}