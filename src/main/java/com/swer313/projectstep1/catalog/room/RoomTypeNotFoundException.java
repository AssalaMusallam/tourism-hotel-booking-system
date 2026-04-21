package com.swer313.projectstep1.catalog.room;

import com.swer313.projectstep1.errors.ResourceNotFoundException;

public class RoomTypeNotFoundException extends ResourceNotFoundException {
    public RoomTypeNotFoundException(Long id) {
        super("RoomType with id " + id + " was not found.");
    }
}