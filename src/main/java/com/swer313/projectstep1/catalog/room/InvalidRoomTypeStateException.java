package com.swer313.projectstep1.catalog.room;

import com.swer313.projectstep1.errors.BusinessValidationException;

public class InvalidRoomTypeStateException extends BusinessValidationException {
    public InvalidRoomTypeStateException(String message) {
        super(message);
    }
}