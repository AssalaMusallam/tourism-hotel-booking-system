package com.swer313.projectstep1.availabilitypricing.availability;

import com.swer313.projectstep1.errors.BadRequestException;

public class AvailabilityDateRangeException extends BadRequestException {

    public AvailabilityDateRangeException(String message) {
        super(message);
    }
}