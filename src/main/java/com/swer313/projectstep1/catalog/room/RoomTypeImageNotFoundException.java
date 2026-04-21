package com.swer313.projectstep1.catalog.room;

import com.swer313.projectstep1.errors.NotFoundException;

public class RoomTypeImageNotFoundException extends NotFoundException {
    public RoomTypeImageNotFoundException(Long id) {
        super("Room image not found with id: " + id);
    }
}