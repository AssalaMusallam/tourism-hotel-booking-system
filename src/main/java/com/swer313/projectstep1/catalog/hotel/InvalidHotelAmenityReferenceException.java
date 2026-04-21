package com.swer313.projectstep1.catalog.hotel;

import com.swer313.projectstep1.errors.BadRequestException;

import java.util.Set;


public class InvalidHotelAmenityReferenceException extends BadRequestException {

    public InvalidHotelAmenityReferenceException(Set<Long> missingIds) {
        super("The following amenity IDs do not exist: " + missingIds);
    }
}