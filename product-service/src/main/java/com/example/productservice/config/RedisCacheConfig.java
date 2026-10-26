
package com.example.productservice.config;

import java.time.Duration;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisCacheConfig {
    
    @Bean 
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) 
                .disableCachingNullValues() 
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())) 
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(RedisSerializer.json())); 
    }

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            RedisCacheConfiguration redisCacheConfiguration) {

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();

        return new AbstractCacheManager() {

            @Override
            protected java.util.Collection<? extends Cache> loadCaches() {
                return redisCacheManager.getCacheNames()
                        .stream()
                        .map(redisCacheManager::getCache)
                        .filter(java.util.Objects::nonNull)
                        .map(FailOpenRedisCache::new) 
                        .toList();
            }

            @Override
            protected Cache getMissingCache(String name) {
                Cache cache = redisCacheManager.getCache(name);

                return cache == null ? null : new FailOpenRedisCache(cache);
            }
        };
    }
}
