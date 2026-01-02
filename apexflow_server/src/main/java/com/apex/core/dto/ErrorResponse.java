package com.apex.core.dto;

/**
 * Error response DTO
 */
public class ErrorResponse {
    private long timestamp;
    private int status;
    private String message;
    private String errorCode;
    private String path;

    public ErrorResponse(int status, String message, String errorCode) {
        this.timestamp = System.currentTimeMillis();
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
    }

    // Getters and setters
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}