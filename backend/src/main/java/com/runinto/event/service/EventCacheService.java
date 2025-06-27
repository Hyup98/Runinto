package com.runinto.event.service;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.runinto.event.dto.cache.EventCacheDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final EventRepository eventRepository;
    // 💡 캐시 키 접두사 정의
    private static final String EVENT_CACHE_PREFIX = "event::";

    public EventCacheService(@Qualifier("cacheRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                             EventRepository eventRepository) {
        this.redisTemplate = redisTemplate;
        this.eventRepository = eventRepository;
    }

    // 캐시에서 그리드 목록 조회 (반환 타입: Map<String, List<EventResponse>>)
    public Map<String, List<EventCacheDto>> findGridsFromCache(List<String> gridIds) {
        if (gridIds == null || gridIds.isEmpty()) {
            return new HashMap<>();
        }
        List<Object> results = redisTemplate.opsForValue().multiGet(gridIds);
        Map<String, List<EventCacheDto>> cachedGrids = new HashMap<>();
        for (int i = 0; i < gridIds.size(); i++) {
            if (results.get(i) != null) {
                // 캐시에서 가져온 데이터를 EventResponse 리스트로 캐스팅
                cachedGrids.put(gridIds.get(i), (List<EventCacheDto>) results.get(i));
            }
        }
        log.info("{}개의 그리드 중 {}개를 캐시에서 찾았습니다.", gridIds.size(), cachedGrids.size());
        return cachedGrids;
    }

    // DB에서 조회 후 DTO로 변환하여 캐시에 저장
    public Map<String, List<EventCacheDto>> findGridsFromDbAndCache(List<String> missedGridIds) {
        // 1. DB에서 Event 목록 조회
        List<Event> eventsFromDb = eventRepository.findByGridIdInWithCategories(missedGridIds);

        // 💡 엔티티를 EventCacheDto로 변환
        List<EventCacheDto> eventCacheDtos = eventsFromDb.stream()
                .map(EventCacheDto::from)
                .toList();

        // 💡 변환된 EventCacheDto를 gridId로 그룹화
        Map<String, List<EventCacheDto>> eventsByGridId = eventCacheDtos.stream()
                .collect(Collectors.groupingBy(EventCacheDto::gridId));

        // 4. 💡 DTO 맵을 Redis에 저장
        if (!eventsByGridId.isEmpty()) {
            Map<String, Object> mapToCache = new HashMap<>(eventsByGridId);
            redisTemplate.opsForValue().multiSet(mapToCache);
            eventsByGridId.keySet().forEach(key -> redisTemplate.expire(key, 10, TimeUnit.MINUTES));
        }
        return eventsByGridId;
    }
    // 💡ID 목록으로 캐시에서 EventCacheDto 조회
    public Map<Long, EventCacheDto> findEventsByIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashMap<>();
        }

        List<String> cacheKeys = eventIds.stream()
                .map(id -> EVENT_CACHE_PREFIX + id)
                .collect(Collectors.toList());

        List<Object> results = redisTemplate.opsForValue().multiGet(cacheKeys);
        Map<Long, EventCacheDto> cachedEvents = new HashMap<>();

        for (int i = 0; i < eventIds.size(); i++) {
            if (results != null && results.get(i) != null) {
                cachedEvents.put(eventIds.get(i), (EventCacheDto) results.get(i));
            }
        }
        log.info("{}개의 ID 중 {}개를 캐시에서 찾았습니다.", eventIds.size(), cachedEvents.size());
        return cachedEvents;
    }

    // 💡 [신규] DTO 목록을 캐시에 저장
    public void saveEventsToCache(List<EventCacheDto> eventDtos) {
        if (eventDtos == null || eventDtos.isEmpty()) {
            return;
        }

        Map<String, Object> mapToCache = eventDtos.stream()
                .collect(Collectors.toMap(
                        dto -> EVENT_CACHE_PREFIX + dto.eventId(), // Key: "event::1"
                        dto -> dto // Value: EventCacheDto 객체
                ));

        redisTemplate.opsForValue().multiSet(mapToCache);
        // 각 키에 만료 시간 설정
        mapToCache.keySet().forEach(key -> redisTemplate.expire(key, 10, TimeUnit.MINUTES));
        log.info("{}개의 이벤트를 캐시에 저장했습니다.", mapToCache.size());
    }

    // 💡 [신규] 개별 이벤트 캐시 무효화
    public void invalidateEventCache(Long eventId) {
        String cacheKey = EVENT_CACHE_PREFIX + eventId;
        log.info("이벤트 캐시 무효화: {}", cacheKey);
        redisTemplate.delete(cacheKey);
    }

    // 특정 그리드 캐시 무효화
    /*public void invalidateGridCache(String gridId) {
        log.info("캐시 무효화: {}", gridId);
        redisTemplate.delete(gridId);
    }*/
}