package com.assistant.aiinterviewassistant.infrastructure.redis;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存对象，使用 Redis String 类型存储。
     *
     * <p>缓存值通过 RedisTemplate 配置的序列化器进行转换。
     *
     * @param <T> 缓存值的类型
     * @param key Redis 键
     * @param value 缓存值
     */
    public <T> void setCacheObject(String key , T value){
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存对象，使用 Redis String 类型存储。
     *
     * <p>缓存值通过 RedisTemplate 配置的序列化器进行转换。
     *
     * @param <T> 缓存值的类型
     * @param key Redis 键
     * @param value 缓存值
     * @param timeout 缓存超时时间
     * @param timeunit 缓存超时时间单位
     */
    public <T> void setCacheObject(String key , T value, long timeout, TimeUnit timeunit){
        redisTemplate.opsForValue().set(key, value, timeout, timeunit);
    }

    /**
     * 判断 key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 获取缓存的对象
     * @param key 键
     * @return 缓存的对象
     * @param <T> 类型
     */
    public <T> T getCacheObject(String key){
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存对象
     * @param key 键
     *@return true 表示删除了已有 Key；false 表示 Key 不存在
     */
    public boolean deleteObject(String key){
        return redisTemplate.delete(key);
    }
}
