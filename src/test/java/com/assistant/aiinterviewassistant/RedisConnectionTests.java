package com.assistant.aiinterviewassistant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RedisConnectionTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void redisIsReachableAndReadWriteWorks() {
        String key = "smoke:test";
        redisTemplate.opsForValue().set(key, "hello");
        String value = redisTemplate.opsForValue().get(key);
        assertEquals("hello", value);
        redisTemplate.delete(key);
    }
}
