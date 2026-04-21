package com.swer313.projectstep1.user;

import com.swer313.projectstep1.errors.BadRequestException;

public class ManagerNotAssignedException extends BadRequestException {

    public ManagerNotAssignedException(Long userId, Long hotelId) {
        super("User " + userId + " is not a manager of hotel " + hotelId);
    }
}