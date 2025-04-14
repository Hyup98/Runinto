package com.runinto.event.service;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;
import com.runinto.event.domain.repository.EventMemoryRepository;
import com.runinto.event.dto.request.FindEventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {
    private EventService eventService;
    private EventMemoryRepository eventMemoryRepository;

    @BeforeEach
    void setUp() {
        eventMemoryRepository = new EventMemoryRepository();

        EventType[] eventTypes = EventType.values(); // [EAT, ACTIVITY, TALKING]

        for (long i = 1; i <= 10; i++) {
            // 순환 방식으로 카테고리 고르게 분포시키기
            EventType type = eventTypes[(int)((i - 1) % eventTypes.length)];

            Set<EventCategory> categories = Set.of(new EventCategory(i, type, i));

            Event event = Event.builder()
                    .eventId(i)
                    .title("이벤트 " + i + " - " + type.name())
                    .description("설명입니다 " + i)
                    .maxParticipants(10)
                    .creationTime(Time.valueOf(LocalTime.now()))
                    .latitude(37.56 + (i * 0.001)) // 위도 37.561 ~ 37.570
                    .longitude(127.01 + (i * 0.001)) // 경도 127.011 ~ 127.020
                    .chatroomId(i)
                    .participants((int) (i % 5))
                    .categories(categories)
                    .build();

            eventMemoryRepository.save(event);
        }

        eventService = new EventService(eventMemoryRepository);
    }


    @Test
    void findByDynamicCondition() {
        // given
        FindEventRequest request = new FindEventRequest(
                37.567, 127.017,   // 북동 (NE)
                37.563, 127.013,   // 남서 (SW)
                List.of(new EventCategory(1L, EventType.ACTIVITY, 1L))
        );

        // when
        List<Event> result = eventService.findByDynamicCondition(request);

        // then
        assertEquals(1, result.size()); // i = 5번 이벤트만 일치

        Event event = result.get(0);
        assertEquals("이벤트 5 - ACTIVITY", event.getTitle());
        assertEquals(EventType.ACTIVITY, event.getEventCategories().iterator().next().getCategory());
        assertTrue(event.getLatitude() >= 37.563 && event.getLatitude() <= 37.567);
        assertTrue(event.getLongitude() >= 127.013 && event.getLongitude() <= 127.017);
    }
}