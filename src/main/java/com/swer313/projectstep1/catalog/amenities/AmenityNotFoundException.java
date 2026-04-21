package com.swer313.projectstep1.catalog.amenities;

import com.swer313.projectstep1.errors.ResourceNotFoundException;

public class AmenityNotFoundException extends ResourceNotFoundException {
    public AmenityNotFoundException(Long id) {
        super("Amenity with id " + id + " was not found.");
    }
}