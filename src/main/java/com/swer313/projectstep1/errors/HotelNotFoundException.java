package com.swer313.projectstep1.errors;

public class HotelNotFoundException extends NotFoundException {
    public HotelNotFoundException(Long id) {
        super("Hotel not found with id=" + id);
    }
}