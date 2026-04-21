package com.swer313.projectstep1.user;

import com.swer313.projectstep1.errors.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
}