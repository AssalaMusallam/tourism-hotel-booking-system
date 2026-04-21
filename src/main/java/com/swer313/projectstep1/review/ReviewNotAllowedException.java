package com.swer313.projectstep1.review;

import com.swer313.projectstep1.errors.BusinessValidationException;

public class ReviewNotAllowedException extends BusinessValidationException {
    public ReviewNotAllowedException(String message) {
        super(message);
    }
}