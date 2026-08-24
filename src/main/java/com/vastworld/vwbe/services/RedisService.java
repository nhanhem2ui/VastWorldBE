package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redis;
    private final ObjectMapper objectMapper;

    //Qualifier for multiples object mapper
    public RedisService(RedisTemplate<String, Object> redis, @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    // increments the value of KEYS[1] by 1.
    // If the key does not exist yet, creates it then increments it to 1.
    // The new value is stored in variable named current
    private static final RedisScript<Long> INCR_WITH_EXPIRE = RedisScript.of("""
    local current = redis.call('INCR', KEYS[1])
    if tonumber(current) == 1 then
      redis.call('PEXPIRE', KEYS[1], ARGV[1])
    end
    return current
    """, Long.class);

    public <T> void set(String key, T value) {
        redis.opsForValue().set(key, value, 1, TimeUnit.HOURS);
    }

    public <T> void set(String key, T value, long timeout, TimeUnit timeUnit) {
        redis.opsForValue().set(key, value, timeout, timeUnit);
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

    public void delete(String key) {
        redis.delete(key);
    }

    public long incrementWithExpiry(String key, Duration window) {
        Long result = redis.execute(
                INCR_WITH_EXPIRE,
                new StringRedisSerializer(),   // args
                new GenericToStringSerializer<>(Long.class), // result
                List.of(key),
                String.valueOf(window.toMillis())
        );
        return result == null ? 0 : result;
    }

    public Long increment(String key) {
        return redis.opsForValue().increment(key);
    }

    public void expire(String key, Duration duration) {
        redis.expire(key, duration);
    }
}
