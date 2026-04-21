package com.swer313.projectstep1.errors;

import java.util.List;
import java.util.Map;

public class ApiError {

    private final String timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<Map<String, String>> errors;

    public ApiError(String timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null);
    }

    public ApiError(
            String timestamp,
            int status,
            String error,
            String message,
            String path,
            List<Map<String, String>> errors
    ) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }

    public String getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public List<Map<String, String>> getErrors() { return errors; }
}