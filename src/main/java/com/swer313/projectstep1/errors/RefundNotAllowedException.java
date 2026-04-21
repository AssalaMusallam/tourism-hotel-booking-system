package com.swer313.projectstep1.errors;


import com.swer313.projectstep1.errors.ConflictException;

public class RefundNotAllowedException extends ConflictException {

    public RefundNotAllowedException(String message) {
        super(message);
    }
}