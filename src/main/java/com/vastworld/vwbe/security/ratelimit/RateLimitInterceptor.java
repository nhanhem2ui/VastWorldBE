package com.vastworld.vwbe.security.ratelimit;

import com.vastworld.vwbe.services.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    private final RedisService redisService;

    public RateLimitInterceptor(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) return true;

        RateLimit rateLimit = resolveRateLimit(handlerMethod);
        if (rateLimit == null) return true;

        String key = "rate-limit:" + handlerMethod.getMethod().getDeclaringClass().getSimpleName()
                + ":" + handlerMethod.getMethod().getName()
                + ":" + clientKey(request);

        long count;
        try {
            count = redisService.incrementWithExpiry(
                    key, Duration.ofMillis(rateLimit.unit().toMillis(rateLimit.duration())));
        } catch (Exception e) {
            log.warn("Rate limiter unavailable, allowing request through: {}", e.getMessage());
            return true; // fail-open
        }

        if (count > rateLimit.limit()) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(rateLimit.unit().toSeconds(rateLimit.duration())));
            response.getWriter().write("Too many requests");
            return false;
        }
        return true;
    }

    private RateLimit resolveRateLimit(HandlerMethod handlerMethod) {
        RateLimit methodLevel = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (methodLevel != null) return methodLevel;
        return handlerMethod.getBeanType().getAnnotation(RateLimit.class);
    }

    private String clientKey(HttpServletRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }
        return "ip:" + request.getRemoteAddr();
    }
}