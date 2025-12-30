package com.apex.api;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base Servlet for API endpoints with comprehensive logging and common functionality
 */
public abstract class BaseServlet extends HttpServlet {

    protected static final Logger logger = LoggerFactory.getLogger(BaseServlet.class);
    protected final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get request client IP address
     */
    protected String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // For multiple IPs, get the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }

    /**
     * Log incoming request details
     */
    protected void logRequest(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? uri + "?" + queryString : uri;

        logger.info("[API_REQUEST] Incoming request. Method: {}, URL: {}, Client IP: {}, User-Agent: {}",
                method, fullUrl, clientIp, request.getHeader("User-Agent"));

        logger.debug("[API_REQUEST] Request headers - Content-Type: {}, Accept: {}, Content-Length: {}",
                request.getHeader("Content-Type"),
                request.getHeader("Accept"),
                request.getHeader("Content-Length"));
    }

    /**
     * Log response details
     */
    protected void logResponse(HttpServletRequest request, HttpServletResponse response,
                               long startTime, int statusCode) {
        long duration = System.currentTimeMillis() - startTime;
        String method = request.getMethod();
        String uri = request.getRequestURI();

        String logLevel = (statusCode >= 200 && statusCode < 400) ? "INFO" :
                (statusCode >= 400 && statusCode < 500) ? "WARN" : "ERROR";

        switch (logLevel) {
            case "INFO":
                logger.info("[API_RESPONSE] Request completed. Method: {}, URI: {}, Status: {}, Duration: {}ms",
                        method, uri, statusCode, duration);
                break;
            case "WARN":
                logger.warn("[API_RESPONSE] Client error. Method: {}, URI: {}, Status: {}, Duration: {}ms",
                        method, uri, statusCode, duration);
                break;
            case "ERROR":
                logger.error("[API_RESPONSE] Server error. Method: {}, URI: {}, Status: {}, Duration: {}ms",
                        method, uri, statusCode, duration);
                break;
        }
    }

    /**
     * Send JSON response
     */
    protected void sendJsonResponse(HttpServletResponse response, int statusCode, Object data)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        String jsonResponse;
        if (data instanceof String) {
            jsonResponse = (String) data;
        } else {
            jsonResponse = objectMapper.writeValueAsString(data);
        }

        response.getWriter().write(jsonResponse);
        logger.debug("[API_RESPONSE] JSON response sent. Status: {}, Length: {}",
                statusCode, jsonResponse.length());
    }

    /**
     * Send error response
     */
    protected void sendErrorResponse(HttpServletResponse response, int statusCode,
                                     String message, String errorCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        ErrorResponse error = new ErrorResponse(statusCode, message, errorCode);
        String jsonError = objectMapper.writeValueAsString(error);

        response.getWriter().write(jsonError);
        logger.warn("[API_ERROR] Error response sent. Status: {}, Message: {}, ErrorCode: {}",
                statusCode, message, errorCode);
    }

    /**
     * Parse JSON request body
     */
    protected <T> T parseJsonBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        String requestBody = extractRequestBody(request);
        logger.debug("[API_REQUEST] Request body: {}", requestBody);

        try {
            return objectMapper.readValue(requestBody, clazz);
        } catch (IOException e) {
            logger.error("[API_ERROR] Failed to parse JSON request body. Error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extract request body as string
     */
    protected String extractRequestBody(HttpServletRequest request) throws IOException {
        return request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
    }

    /**
     * Error response DTO
     */
    public static class ErrorResponse {
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
}
