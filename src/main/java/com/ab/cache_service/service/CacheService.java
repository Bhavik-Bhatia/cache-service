package com.ab.cache_service.service;

import com.ab.cache_service.constants.CacheConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This particular class provides methods for caching for different operations
 */
@Service
public class CacheService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    public enum CacheOperation {
        INSERT,
        UPDATE,
        DELETE,
        FETCH
    }

    public List<?> cacheOps(Map<?, ?> dataMap, CacheOperation cacheOperation) {
        switch (cacheOperation) {
            case FETCH:
                return fetchFromCache(dataMap);

            case INSERT, UPDATE:
                return insertFromCache(dataMap);

            case DELETE:
                break;

            default:
                throw new RuntimeException(CacheConstants.UNIDENTIFIED_OPERATION);

        }
        return new ArrayList<>();
    }

    private List<?> insertFromCache(Map<?, ?> dataMap) {
        try {
            for (Map.Entry<?, ?> data : dataMap.entrySet()) {
                if (validateKey((String) data.getKey())) {
                    redisTemplate.opsForValue().set(data.getKey(), data.getValue());
                } else {
                    LOGGER.error("Plain cache key is not allowed");
                }
            }
        } catch (Exception e) {
            LOGGER.error("ERROR while inserting in cache {}", e.getMessage());
            return Collections.singletonList(false);
        }
        return Collections.singletonList(true);
    }

    private List<?> fetchFromCache(Map<?, ?> dataMap) {
        Object dataFromCache = null;
        try {
            for (Map.Entry<?, ?> data : dataMap.entrySet()) {
                if (validateKey((String) data.getKey())) {
                    dataFromCache = redisTemplate.opsForValue().get(data.getKey());
                } else {
                    LOGGER.error("Plain cache key is not allowed");
                }
            }
        } catch (Exception e) {
            LOGGER.error("ERROR while fetching in cache {}", e.getMessage());
        }
        return Collections.singletonList(dataFromCache);
    }

    /**
     * Method validates if there is hash present in key, plain value should not be cache key
     *
     * @param key cache key
     * @return boolean
     */
    private boolean validateKey(String key) {
        return key.contains("#");
    }
}