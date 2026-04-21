package com.swer313.projectstep1.errors;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseApiException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);  // 409 صريح
    }
}