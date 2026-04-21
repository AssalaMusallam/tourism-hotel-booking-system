package com.swer313.projectstep1.catalog.hotel;

import com.swer313.projectstep1.errors.ResourceNotFoundException;

public class HotelImageNotFoundException extends ResourceNotFoundException {
    public HotelImageNotFoundException(Long imageId) {
        super("Hotel image not found with id: " + imageId);
    }
}