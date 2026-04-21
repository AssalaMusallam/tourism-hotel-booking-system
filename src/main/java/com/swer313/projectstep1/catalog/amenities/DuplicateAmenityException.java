package com.swer313.projectstep1.catalog.amenities;

import com.swer313.projectstep1.errors.DuplicateResourceException;

public class DuplicateAmenityException extends DuplicateResourceException {
    public DuplicateAmenityException(String name) {
        super("Amenity with name '" + name + "' already exists.");
    }
}