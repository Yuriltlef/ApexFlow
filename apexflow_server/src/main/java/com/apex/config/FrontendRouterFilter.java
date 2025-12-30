package com.apex.config;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vue Router History Mode Filter
 * Forwards all frontend routing requests to index.html, allowing Vue Router to handle routing
 */
@WebFilter("/*")
public class FrontendRouterFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(FrontendRouterFilter.class);

    // Static resource extensions (not forwarded)
    private static final Set<String> STATIC_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".js", ".css", ".html", ".htm", ".json", ".xml",
            ".jpg", ".jpeg", ".png", ".gif", ".svg", ".ico",
            ".woff", ".woff2", ".ttf", ".eot", ".otf",
            ".mp4", ".webm", ".mp3", ".wav", ".ogg"
    ));

    // Path prefixes that don't need processing
    private static final Set<String> EXCLUDED_PREFIXES = new HashSet<>(Arrays.asList(
            "/api/",        // API endpoints
            "/assets/",     // Static assets
            "/static/",     // Static resources
            "/css/",        // CSS files
            "/js/",         // JavaScript files
            "/img/",        // Image files
            "/upload/",     // Uploaded files
            "/error",       // Error pages
            "/favicon.ico"  // Favicon
    ));

    // Static file path pattern
    private static final Pattern STATIC_FILE_PATTERN =
            Pattern.compile(".*\\.(js|css|html|htm|json|xml|jpg|jpeg|png|gif|svg|ico|woff|woff2|ttf|eot|otf|mp4|webm|mp3|wav|ogg)$",
                    Pattern.CASE_INSENSITIVE);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("[FRONTEND_FILTER] FrontendRouterFilter initialized successfully");
        logger.debug("[FRONTEND_FILTER] Static extensions registered: {}", STATIC_EXTENSIONS.size());
        logger.debug("[FRONTEND_FILTER] Excluded prefixes: {}", EXCLUDED_PREFIXES);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        logger.debug("[FRONTEND_FILTER] Processing request. URI: {}, Context Path: {}, Path: {}",
                requestURI, contextPath, path);

        // Check if path should be excluded
        if (shouldExclude(path)) {
            logger.debug("[FRONTEND_FILTER] Path is excluded from filtering. Path: {}", path);
            chain.doFilter(request, response);
            return;
        }

        // Check if it's a static resource
        if (isStaticResource(path)) {
            logger.debug("[FRONTEND_FILTER] Static resource detected. Path: {}", path);
            chain.doFilter(request, response);
            return;
        }

        // Check if it's an API request
        if (path.startsWith("/api/")) {
            logger.debug("[FRONTEND_FILTER] API request detected. Path: {}", path);
            chain.doFilter(request, response);
            return;
        }

        // Check if it's an error page
        if (path.startsWith("/error")) {
            logger.debug("[FRONTEND_FILTER] Error page request. Path: {}", path);
            chain.doFilter(request, response);
            return;
        }

        // Check if it's a WebSocket request
        if (isWebSocketRequest(httpRequest)) {
            logger.debug("[FRONTEND_FILTER] WebSocket request detected. Path: {}", path);
            chain.doFilter(request, response);
            return;
        }

        // All other requests are forwarded to index.html
        String forwardPath = "/index.html";
        logger.info("[FRONTEND_FILTER] Forwarding frontend route to index.html. Original path: {}, Forward path: {}",
                path, forwardPath);

        RequestDispatcher dispatcher = httpRequest.getRequestDispatcher(forwardPath);

        // Add cache control headers to prevent browser caching of index.html
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setDateHeader("Expires", 0);

        dispatcher.forward(request, response);
    }

    @Override
    public void destroy() {
        logger.info("[FRONTEND_FILTER] FrontendRouterFilter destroyed");
    }

    /**
     * Check if the path should be excluded from filtering
     */
    private boolean shouldExclude(String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return false;
        }

        for (String prefix : EXCLUDED_PREFIXES) {
            if (path.startsWith(prefix)) {
                logger.trace("[FRONTEND_FILTER] Path matches excluded prefix. Path: {}, Prefix: {}", path, prefix);
                return true;
            }
        }

        return false;
    }

    /**
     * Check if the path is a static resource
     */
    private boolean isStaticResource(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        // Method 1: Check by extension
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex > 0) {
            String extension = path.substring(dotIndex).toLowerCase();
            if (STATIC_EXTENSIONS.contains(extension)) {
                logger.trace("[FRONTEND_FILTER] Static resource by extension. Path: {}, Extension: {}", path, extension);
                return true;
            }
        }

        // Method 2: Check by regex pattern
        if (STATIC_FILE_PATTERN.matcher(path).matches()) {
            logger.trace("[FRONTEND_FILTER] Static resource by pattern. Path: {}", path);
            return true;
        }

        return false;
    }

    /**
     * Check if it's a WebSocket request
     */
    private boolean isWebSocketRequest(HttpServletRequest request) {
        String upgradeHeader = request.getHeader("Upgrade");
        String connectionHeader = request.getHeader("Connection");

        boolean isWebSocket = "websocket".equalsIgnoreCase(upgradeHeader) &&
                "Upgrade".equalsIgnoreCase(connectionHeader);

        if (isWebSocket) {
            logger.debug("[FRONTEND_FILTER] WebSocket upgrade detected");
        }

        return isWebSocket;
    }
}
