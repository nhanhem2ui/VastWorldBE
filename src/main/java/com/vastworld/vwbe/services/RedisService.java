package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redis;
    private final ObjectMapper objectMapper;

    public RedisService(RedisTemplate<String, Object> redis, @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    /**
     *Override value if exists
     */
    public <T> void set(String key, T value) {
        redis.opsForValue().set(key, value);
    }

    public boolean setIfAbsent(String key, Object value
    ) {
        return Boolean.TRUE.equals(
                redis.opsForValue().setIfAbsent(key, value)
        );
    }

    public <T> T get(String key, TypeReference<T> typeRef) {
        Object raw = redis.opsForValue().get(key);
        if (raw == null) return null;
        return objectMapper.convertValue(raw, typeRef);
    }
}
