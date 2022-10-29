package com.example.UbitAutoTrading.redisTest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTemplateService{
    private final RedisTemplate<String, Object> redisTemplate;

    public void save() {
        ValueOperations<String, Object> value = redisTemplate.opsForValue();
        value.set("testKey", "testValue");
    }
}