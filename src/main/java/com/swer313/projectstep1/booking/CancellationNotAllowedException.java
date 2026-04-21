package com.swer313.projectstep1.booking;

import com.swer313.projectstep1.errors.BaseApiException;
import org.springframework.http.HttpStatus;

class CancellationNotAllowedException extends BaseApiException {
    public CancellationNotAllowedException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}