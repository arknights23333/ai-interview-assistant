package com.assistant.aiinterviewassistant.infrastructure.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * Redis 通用对象缓存的序列化配置。
 *
 * <p>Key 和 Hash Key 使用字符串，Value 和 Hash Value 使用 JSON；
 * 字符串会话标识、验证码等数据仍可使用 Boot 自动配置的 StringRedisTemplate。
 */
@Configuration
public class RedisConfig {

    /**
     * 配置通用对象 RedisTemplate，替代 Boot 默认的同名模板。
     *
     * <p>JSON 中保留类型信息，使业务对象、集合等数据能够按原类型读取；
     * 使用独立的 JSON 序列化器，不修改 HTTP 接口使用的 JSON 配置。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        // 允许还原项目业务对象及常用 JDK 值类型、集合和数组。
        BasicPolymorphicTypeValidator typeValidator =
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.assistant.aiinterviewassistant.")
                        .allowIfSubType("java.util.")
                        .allowIfSubType("java.time.")
                        .allowIfSubType(String.class)
                        .allowIfSubType(Number.class)
                        .allowIfSubType(Boolean.class)
                        .allowIfSubType(Character.class)
                        .allowIfSubTypeIsArray()
                        .build();

        // 使用 Jackson 3，写入类型信息，并发现类路径上的扩展模块。
        GenericJacksonJsonRedisSerializer jsonSerializer =
                GenericJacksonJsonRedisSerializer.builder()
                        .enableDefaultTyping(typeValidator)
                        .customize(builder -> builder.findAndAddModules())
                        .build();

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 普通 Key 和 Hash 内部字段名均保存为可读字符串。
        template.setKeySerializer(StringRedisSerializer.UTF_8);
        template.setHashKeySerializer(StringRedisSerializer.UTF_8);

        // 普通 Value 和 Hash 内部字段值均使用带类型信息的 JSON。
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        return template;
    }
}
