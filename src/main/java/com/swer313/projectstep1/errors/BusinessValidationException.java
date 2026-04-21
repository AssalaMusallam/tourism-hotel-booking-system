package com.swer313.projectstep1.errors;

import org.springframework.http.HttpStatus;

public class BusinessValidationException extends BaseApiException {

    public BusinessValidationException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}