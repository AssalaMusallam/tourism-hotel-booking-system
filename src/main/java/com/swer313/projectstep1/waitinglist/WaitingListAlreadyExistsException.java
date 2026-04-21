// WaitingListAlreadyExistsException.java
package com.swer313.projectstep1.waitinglist;

import com.swer313.projectstep1.errors.DuplicateResourceException;

public class WaitingListAlreadyExistsException extends DuplicateResourceException {
    public WaitingListAlreadyExistsException(String guestEmail, Long roomTypeId) {
        super("Guest '" + guestEmail + "' is already on the waiting list " +
                "for room type id: " + roomTypeId);
    }
}