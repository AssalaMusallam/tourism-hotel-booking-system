package com.swer313.projectstep1.catalog.hotel;

import com.swer313.projectstep1.errors.DuplicateResourceException;


public class DuplicateHotelNameException extends DuplicateResourceException {

    public DuplicateHotelNameException(String name) {
        super("A hotel with the name '" + name + "' already exists.");
    }
}