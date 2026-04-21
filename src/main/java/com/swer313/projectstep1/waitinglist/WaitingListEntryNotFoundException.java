// WaitingListEntryNotFoundException.java
package com.swer313.projectstep1.waitinglist;

import com.swer313.projectstep1.errors.ResourceNotFoundException;

public class WaitingListEntryNotFoundException extends ResourceNotFoundException {
    public WaitingListEntryNotFoundException(Long id) {
        super("Waiting list entry with id " + id + " was not found.");
    }
}