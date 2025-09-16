package com.flagship.inventory.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Cache Configuration for Inventory Service
 * <p>
 * Configures Redis-based caching for high-performance inventory access. This includes: - Redis
 * cache manager - Cache configurations - Serialization settings - TTL configurations
 */
@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(30))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
            new GenericJackson2JsonRedisSerializer()))
        .disableCachingNullValues();

    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(config)
        .withCacheConfiguration("products",
            config.entryTtl(Duration.ofMinutes(60)))
        .withCacheConfiguration("inventory",
            config.entryTtl(Duration.ofMinutes(15)))
        .withCacheConfiguration("categories",
            config.entryTtl(Duration.ofHours(2)))
        .withCacheConfiguration("brands",
            config.entryTtl(Duration.ofHours(2)))
        .build();
  }
}
