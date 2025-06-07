package com.runinto.event.service;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.repository.EventRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class EventCacheService {

    private final RedisTemplate<String, Object> redisTemplate; // 캐시용 RedisTemplate
    private final EventRepository eventRepository;

    // 생성자에서 @Qualifier를 사용하여 "cacheRedisTemplate" 빈을 주입받습니다.
    public EventCacheService(@Qualifier("cacheRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                             EventRepository eventRepository) {
        this.redisTemplate = redisTemplate;
        this.eventRepository = eventRepository;
    }

    // 캐시에서 그리드 목록 조회
    public Map<String, List<Event>> findGridsFromCache(List<String> gridIds) {
        // MGET으로 한번에 여러 그리드를 가져옴
        List<Object> results = redisTemplate.opsForValue().multiGet(gridIds);
        Map<String, List<Event>> cachedGrids = new HashMap<>();
        for (int i = 0; i < gridIds.size(); i++) {
            if (results.get(i) != null) {
                cachedGrids.put(gridIds.get(i), (List<Event>) results.get(i));
            }
        }
        return cachedGrids;
    }

    // DB에서 조회 후 캐시에 저장
    public Map<String, List<Event>> findGridsFromDbAndCache(List<String> missedGridIds) {
        // 새로운 Repository 메소드 필요 (findByGridIdIn)
        List<Event> eventsFromDb = eventRepository.findByGridIdIn(missedGridIds);

        // 그리드 ID별로 이벤트 목록을 그룹화
        Map<String, List<Event>> eventsByGridId = eventsFromDb.stream()
                .collect(Collectors.groupingBy(Event::getGridId));

        // Redis에 MSET으로 한번에 저장
        if (!eventsByGridId.isEmpty()) {
            redisTemplate.opsForValue().multiSet(eventsByGridId.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey(),
                            entry -> (Object)entry.getValue()
                    ))
            );
            // TTL 설정 (예: 10분)
            eventsByGridId.keySet().forEach(key -> redisTemplate.expire(key, 10, TimeUnit.MINUTES));
        }
        return eventsByGridId;
    }

    // 특정 그리드 캐시 무효화
    public void invalidateGridCache(String gridId) {
        redisTemplate.delete(gridId);
    }
}