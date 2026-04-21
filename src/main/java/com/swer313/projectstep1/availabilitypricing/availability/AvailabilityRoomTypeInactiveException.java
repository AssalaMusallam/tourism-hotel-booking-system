package com.swer313.projectstep1.availabilitypricing.availability;

import com.swer313.projectstep1.errors.BusinessValidationException;

public class AvailabilityRoomTypeInactiveException extends BusinessValidationException {

    public AvailabilityRoomTypeInactiveException(Long roomTypeId) {
        super("RoomType with id " + roomTypeId
                + " is inactive and cannot be checked for availability.");
    }
}