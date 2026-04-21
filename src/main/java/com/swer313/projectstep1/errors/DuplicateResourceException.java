package com.swer313.projectstep1.errors;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BaseApiException {

    public DuplicateResourceException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}