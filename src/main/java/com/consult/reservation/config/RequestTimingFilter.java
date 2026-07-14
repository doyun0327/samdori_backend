package com.consult.reservation.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** API 응답 시간 로그 — 느린 구간 확인용 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Slf4j
public class RequestTimingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            String path = request.getRequestURI();
            if (path.startsWith("/api/")) {
                long elapsed = System.currentTimeMillis() - start;
                if (elapsed >= 500) {
                    log.warn("[slow] {} {} {}ms status={}",
                            request.getMethod(), path, elapsed, response.getStatus());
                } else {
                    log.info("[timing] {} {} {}ms", request.getMethod(), path, elapsed);
                }
            }
        }
    }
}
