package com.runinto.messaging.consumer;

import com.runinto.event.service.EventCacheService;
import com.runinto.kafka.dto.CacheUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheUpdateConsumer {

    private final EventCacheService eventCacheService;

    @KafkaListener(topics = "cache-management-topic", groupId = "cache-management-group")
    public void consume(CacheUpdateMessage message) {
        log.info("Consumed message for cache update: gridId={}", message.getGridId());

        // 메시지 내용에 따라 적절한 캐시 작업을 수행
        if ("INVALIDATE_GRID".equals(message.getAction())) {
            eventCacheService.invalidateGridCache(message.getGridId());
        }
    }
}