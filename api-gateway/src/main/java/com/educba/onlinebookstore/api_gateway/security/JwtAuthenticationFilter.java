package com.educba.onlinebookstore.api_gateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtAuthenticationFilter  extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();


    public JwtAuthenticationFilter (JwtUtil jwtUtil) {
         this.jwtUtil = jwtUtil;
     }

     private static final List<String> PUBLIC_PATHS = List.of(
             "/api/auth/register",
             "/api/auth/login"
     );

     private static final List<String> PUBLIC_READ_PATHS =  List.of("/api/products/**");


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if(isPublic(path, method)){
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing or malformed Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        if(!jwtUtil.isTokenValid(token)){
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put("X-User-Id", String.valueOf(jwtUtil.extractUserId(token)));
        extraHeaders.put("X-Username", jwtUtil.extractUsername(token));
        extraHeaders.put("X-User-Role", jwtUtil.extractRole(token));

        HttpServletRequest wrappedRequest = new HeaderMapRequestWrapper(request, extraHeaders);

        filterChain.doFilter(wrappedRequest, response);


    }

    private boolean isPublic(String path, String method) {

        boolean matchesFullyPublic = PUBLIC_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        boolean matchesPublicRead = "GET".equalsIgnoreCase(method) &&
                PUBLIC_READ_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));

        return matchesFullyPublic || matchesPublicRead;
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setHeader("X-Auth-Error", message);
    }


    /**
     * Wraps the incoming request so downstream handlers (and the gateway's
     * own HTTP forwarding) see our extra identity headers alongside the
     * original ones — HttpServletRequest is otherwise read-only.
     */
    private static class HeaderMapRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String> extraHeaders;

        public HeaderMapRequestWrapper(HttpServletRequest request, Map<String, String> extraHeaders) {
            super(request);
            this.extraHeaders = extraHeaders;
        }

        @Override
        public String getHeader(String name) {
            String value = extraHeaders.get(name);
            return value != null ? value : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            names.addAll(extraHeaders.keySet());
            return Collections.enumeration(names);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (extraHeaders.containsKey(name)) {
                return Collections.enumeration(List.of(extraHeaders.get(name)));
            }
            return super.getHeaders(name);
        }
    }


}
