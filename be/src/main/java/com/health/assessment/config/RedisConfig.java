package com.health.assessment.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 缓存配置
 *
 * 定义序列化方式和缓存策略，各业务缓存的 TTL 可在此统一管理。
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 共享的 JSON 序列化器实例（redisTemplate 和 cacheManager 共用，确保类型信息一致）
     */
    private Jackson2JsonRedisSerializer<Object> sharedJsonSerializer() {
        return buildJsonSerializer();
    }

    /**
     * 配置 RedisTemplate，使用 String key + JSON value 序列化
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        Jackson2JsonRedisSerializer<Object> jsonSerializer = sharedJsonSerializer();

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置 CacheManager，设置不同缓存的 TTL
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Object> jsonSerializer = sharedJsonSerializer();
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues()
                .prefixCacheNameWith("health:");

        // 各模块自定义 TTL
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        // 健康内容：发布后较稳定，缓存 1 小时
        configMap.put("healthContent", defaultConfig.entryTtl(Duration.ofHours(1)));
        configMap.put("healthContents", defaultConfig.entryTtl(Duration.ofHours(1)));
        // 医学概念/知识图谱：变化少，缓存 6 小时
        configMap.put("medicalConcepts", defaultConfig.entryTtl(Duration.ofHours(6)));
        configMap.put("conceptRelations", defaultConfig.entryTtl(Duration.ofHours(6)));
        // 问卷数据：缓存 30 分钟
        configMap.put("questionnaires", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        // 系统统计：缓存 10 分钟
        configMap.put("statistics", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        // 用户信息：缓存 15 分钟
        configMap.put("userProfile", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        // 健康建议：缓存 30 分钟
        configMap.put("suggestions", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        // 用户信息：缓存 15 分钟
        configMap.put("user", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        // 评测报告：缓存 10 分钟（评测后会清除）
        configMap.put("assessment", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configMap)
                .build();
    }

    private Jackson2JsonRedisSerializer<Object> buildJsonSerializer() {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        serializer.setObjectMapper(om);
        return serializer;
    }
}

