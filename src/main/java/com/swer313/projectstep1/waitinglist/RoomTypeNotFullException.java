// RoomTypeNotFullException.java
package com.swer313.projectstep1.waitinglist;

import com.swer313.projectstep1.errors.BadRequestException;

public class RoomTypeNotFullException extends BadRequestException {
    public RoomTypeNotFullException(Long roomTypeId) {
        super("Room type id: " + roomTypeId +
                " still has available units. No need to join the waiting list.");
    }
}