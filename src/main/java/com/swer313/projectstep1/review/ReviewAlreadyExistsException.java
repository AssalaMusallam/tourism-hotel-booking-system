package com.swer313.projectstep1.review;

import com.swer313.projectstep1.errors.DuplicateResourceException;

public class ReviewAlreadyExistsException extends DuplicateResourceException {
    public ReviewAlreadyExistsException(Long bookingId) {
        super("A review already exists for booking id: " + bookingId);
    }
}