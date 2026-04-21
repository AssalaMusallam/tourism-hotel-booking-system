package com.swer313.projectstep1.user;

import com.swer313.projectstep1.errors.ConflictException;

public class ManagerAlreadyAssignedException extends ConflictException {

    public ManagerAlreadyAssignedException(Long userId, Long hotelId) {
        super("User " + userId + " is already a manager of hotel " + hotelId);
    }
}