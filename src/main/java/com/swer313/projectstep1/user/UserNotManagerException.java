package com.swer313.projectstep1.user;

import com.swer313.projectstep1.errors.BadRequestException;

public class UserNotManagerException extends BadRequestException {

    public UserNotManagerException(Long userId) {
        super("User " + userId + " does not have MANAGER role");
    }
}