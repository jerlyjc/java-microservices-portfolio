
package com.example.productservice.config;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

public class FailOpenRedisCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(FailOpenRedisCache.class);

    private final Cache delegate; 

    private final AtomicBoolean redisUnavailable = new AtomicBoolean(false);

    public FailOpenRedisCache(Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

     
    private boolean prepareRedis() {
        if (!redisUnavailable.get()) {
            return true;
        }

        synchronized (redisUnavailable) {
            if (!redisUnavailable.get()) {
                return true;
            }

            try {
                delegate.clear();
                redisUnavailable.set(false);

                log.warn(
                        "Redis recovered. Cache '{}' was cleared before normal cache use resumes",
                        getName());

                return true;
            } catch (RuntimeException ex) {
                log.warn(
                        "Redis is still unavailable; bypassing cache '{}'",
                        getName());

                return false;
            }
        }
    }

    private void markRedisUnavailable(
            Object key, String operation, RuntimeException ex) {

        if (redisUnavailable.compareAndSet(false, true)) {
            log.warn(
                    "Redis {} failed for cache '{}' and key '{}'; enabling fail-open mode",
                    operation, getName(), key, ex);
        }
    }
    @Override
    public ValueWrapper get(Object key) { 

        if (!prepareRedis()) {
            return null;
        }

        try {
            return delegate.get(key);
        } catch (RuntimeException ex) {
            markRedisUnavailable(key, "read", ex);
            return null;
        }
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
         if (!prepareRedis()) {
            return null;
        }
        try {
            return delegate.get(key, type);
        } catch (RuntimeException ex) {
             markRedisUnavailable(key, "typed read", ex);
            log.warn("Redis cache read failed for key '{}'; treating as a cache miss.",
                    key, ex);
            return null;
        }
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
         if (!prepareRedis()) {
            return loadFromSource(key, valueLoader);
        }
                                                  
        try {
            return delegate.get(key, valueLoader);
        } catch (Cache.ValueRetrievalException ex) {
           
            throw ex;
        } catch (RuntimeException ex) {
            markRedisUnavailable(key, "cache loading", ex);
            return loadFromSource(key, valueLoader);
        }
         
    }
    private <T> T loadFromSource(
            Object key, Callable<T> valueLoader) {

        log.warn(
                "Loading cache key '{}' from the source because Redis is unavailable",
                key);

        try {
            return valueLoader.call();
        } catch (Exception ex) {
            throw new Cache.ValueRetrievalException(
                    key, valueLoader, ex);
        }
    }
    @Override
    public void put(Object key, Object value) { 
        if (!prepareRedis()) {
            return;
        }                                        
        try {
            delegate.put(key, value);
        } catch (RuntimeException ex) {
            markRedisUnavailable(key, "write", ex);
        }
    }

    @Override
    public void evict(Object key) {
        if (!prepareRedis()) {
            log.warn(
                    "Skipping Redis eviction for key '{}' while Redis is unavailable",
                    key);
            return;
        }

        try {
            delegate.evict(key);
        } catch (RuntimeException ex) {    
            markRedisUnavailable(key, "eviction", ex);
        }
    }

    @Override
    public void clear() {
        if (!prepareRedis()) {
            return;
        }
        try {
            delegate.clear();
        } catch (RuntimeException ex) {
            markRedisUnavailable("*", "clear", ex);
        }
    }
}
