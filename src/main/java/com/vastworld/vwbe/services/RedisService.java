package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redis;
    private final ObjectMapper objectMapper;

    //Qualifier for multiples object mapper
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

    public <T> boolean setIfAbsent(String key, T value
    ) {
        return Boolean.TRUE.equals(
                redis.opsForValue().setIfAbsent(key, value)
        );
    }

    public <T> T get(String key, TypeReference<T> typeRef) {
        //raw: LinkedHashMap
        Object raw = redis.opsForValue().get(key);
        if (raw == null) return null;
        //convert to <T>
        return objectMapper.convertValue(raw, typeRef);
    }
    public Long increment(String key) {
        return redis.opsForValue().increment(key);
    }

    public void expire(String key, Duration duration) {
        redis.expire(key, duration);
    }

}
