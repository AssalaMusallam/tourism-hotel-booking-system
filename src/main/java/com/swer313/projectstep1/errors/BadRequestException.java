package com.swer313.projectstep1.errors;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseApiException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}