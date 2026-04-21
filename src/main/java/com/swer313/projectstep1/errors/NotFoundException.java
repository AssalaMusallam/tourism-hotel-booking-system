package com.swer313.projectstep1.errors;

import org.springframework.http.HttpStatus;


public class NotFoundException extends BaseApiException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);  // 404 صريح
    }
}
