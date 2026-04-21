package com.swer313.projectstep1.catalog.room;

import com.swer313.projectstep1.errors.BadRequestException;

public class InvalidAmenityReferenceException extends BadRequestException {
    public InvalidAmenityReferenceException() {
        super("One or more amenityIds are invalid.");
    }
}