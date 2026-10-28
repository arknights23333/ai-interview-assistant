package com.assistant.aiinterviewassistant.infrastructure.redis.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
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

    /**
     * 执行redis脚本
     * @param script 脚本内容
     * @param resultType 返回类型
     * @param keys 脚本访问键
     * @param args 脚本参数
     * @return 执行结果
     * @param <T>返回类型
     */
    public <T> T excuteScript(String script, Class<T> resultType, List<String> keys,Object... args){
        DefaultRedisScript<T> redisScript = new DefaultRedisScript<>(script, resultType);
        return redisTemplate.execute(redisScript, keys, args);
    }

    /**
     * 当缓存值与预期值匹配时，原子地删除该键。
     *
     * @param key Redis 键
     * @param expectedValue 预期的字符串值
     * @return true 表示匹配并已删除；false 表示键不存在或值不匹配
     */
    public boolean deleteIfValueMatches(String key, String expectedValue){
        String scriptText = """ 
                if redis.call('get',KEY[1]) == AGV[1] then
                return redis.call('del',KEY[1])
                end
                return 0
                """;
        Long result= excuteScript(scriptText ,Long.class, Collections.singletonList(key));
        return result!=null&&result!=1L;

    }
}
