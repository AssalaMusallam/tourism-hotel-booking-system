package com.swer313.projectstep1.review;

import com.swer313.projectstep1.errors.ResourceNotFoundException;

public class ReviewNotFoundException extends ResourceNotFoundException {
    public ReviewNotFoundException(Long id) {
        super("Review with id " + id + " was not found.");
    }
}