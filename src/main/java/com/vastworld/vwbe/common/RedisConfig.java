package com.vastworld.vwbe.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.annotation.Documented;

@Configuration
public class RedisConfig {

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        //serialize LocalDate, LocalDateTime
        mapper.registerModule(new JavaTimeModule());
        //timestamp = "2026-01-10T15:00:00"
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper)
    {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);


        RedisSerializer<Object> serializer = new RedisSerializer<>() {
            //serialize Player -> byte[]
            @Override @NullMarked
            public byte[] serialize(@Nullable Object value) {
                if (value == null) return new byte[0];
                try {
                    return redisObjectMapper.writeValueAsBytes(value);
                } catch (Exception e) {
                    throw new RuntimeException("Redis serialization error", e);
                }
            }

            @Override
            //deserialize byte[] -> Player
            public Object deserialize(byte[] bytes) {
                if (bytes == null || bytes.length == 0) return null;
                try {
                    return redisObjectMapper.readValue(bytes, Object.class);
                } catch (Exception e) {
                    throw new RuntimeException("Redis deserialization error", e);
                }
            }
        };

        //serialize key
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        //serialize value
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
