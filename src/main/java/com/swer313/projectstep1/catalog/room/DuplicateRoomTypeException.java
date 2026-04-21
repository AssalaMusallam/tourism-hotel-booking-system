package com.swer313.projectstep1.catalog.room;

import com.swer313.projectstep1.errors.DuplicateResourceException;

public class DuplicateRoomTypeException extends DuplicateResourceException {
    public DuplicateRoomTypeException(Long hotelId, String name) {
        super("RoomType with name '" + name + "' already exists for hotel id " + hotelId + ".");
    }
}