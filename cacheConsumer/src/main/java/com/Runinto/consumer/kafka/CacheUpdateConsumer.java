package com.Runinto.consumer.kafka;

import common.kafka.dto.CacheUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheUpdateConsumer {

    @Qualifier("cacheRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = "cache-management-topic", groupId = "cache-management-group")
    public void consume(CacheUpdateMessage message) {
        log.info("Consumed message for cache update: gridId={}, action={}", message.getGridId(), message.getAction());

        if ("INVALIDATE_GRID".equals(message.getAction())) {
            redisTemplate.delete(message.getGridId());
            log.info("Cache invalidated for gridId: {}", message.getGridId());
        }
    }
}