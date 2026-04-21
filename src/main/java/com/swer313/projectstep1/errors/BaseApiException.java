package com.swer313.projectstep1.errors;

import org.springframework.http.HttpStatus;

public abstract class BaseApiException extends RuntimeException {

    private final HttpStatus status;

    // FIX: الـ constructor بدون status — نحذفه أو نجعله protected
    // لأنه يعطي قيمة افتراضية BAD_REQUEST وهاد مُضلّل
    // أي exception جديدة لازم تحدد الـ status صراحةً
    protected BaseApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}