package com.swer313.projectstep1.catalog.hotel;

import com.swer313.projectstep1.errors.ResourceNotFoundException;


public class HotelNotFoundException extends ResourceNotFoundException {

    public HotelNotFoundException(Long id) {
        super("Hotel not found with id: " + id);
    }
}