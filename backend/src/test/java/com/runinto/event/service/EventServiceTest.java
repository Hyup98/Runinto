package com.runinto.event.service;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;
import com.runinto.event.domain.repository.EventMemoryRepository;
import com.runinto.event.dto.request.FindEventRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;


@Slf4j
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventMemoryRepository eventMemoryRepository;

    @InjectMocks
    private EventService eventService;


    private Event createMockEvent(Long id, EventType type, double lat, double lng) {
        return Event.builder()
                .eventId(id)
                .title("이벤트 " + id + " - " + type.name())
                .description("설명 " + id)
                .latitude(lat)
                .longitude(lng)
                .categories(Set.of(new EventCategory(id, type, id)))
                .build();
    }

    @Test
    @DisplayName("위치만 일치하고 카테고리는 불일치 → 결과 없음")
    void locationMatchesButCategoryDoesNot() {
        // given
        Event event = createMockEvent(1L, EventType.TALKING, 37.565, 127.015);
        FindEventRequest request = new FindEventRequest(
                37.567, 127.017, 37.563, 127.013,
                Set.of(EventType.EAT)
        );

        lenient().when(eventMemoryRepository.findAll()).thenReturn(List.of(event));
        lenient().when(eventMemoryRepository.findByArea(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(event));
        lenient().when(eventMemoryRepository.findByCategory(anySet()))
                .thenReturn(List.of());

        // when
        List<Event> result = eventService.findByDynamicCondition(request);

        // then
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("카테고리는 일치하지만 위치는 벗어남 → 결과 없음")
    void categoryMatchesButLocationDoesNot() {
        // given
        Event event = createMockEvent(2L, EventType.ACTIVITY, 38.000, 128.000); // 범위 밖
        FindEventRequest request = new FindEventRequest(
                37.567, 127.017, 37.563, 127.013,
                Set.of(EventType.ACTIVITY)
        );

        lenient().when(eventMemoryRepository.findAll()).thenReturn(List.of(event));
        lenient().when(eventMemoryRepository.findByArea(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of()); // 위치 불일치
        lenient().when(eventMemoryRepository.findByCategory(anySet()))
                .thenReturn(List.of(event)); // 카테고리는 일치

        // when
        List<Event> result = eventService.findByDynamicCondition(request);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("조건 모두 일치하는 복수 이벤트 반환")
    void multipleEventsMatchingConditions() {
        Event e1 = createMockEvent(1L, EventType.EAT, 37.565, 127.015);
        Event e2 = createMockEvent(2L, EventType.EAT, 37.566, 127.016);
        FindEventRequest request = new FindEventRequest(
                37.567, 127.017, 37.563, 127.013,
                Set.of(EventType.EAT)
        );

        lenient().when(eventMemoryRepository.findAll()).thenReturn(List.of(e1, e2));
        lenient().when(eventMemoryRepository.findByArea(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(e1, e2));
        lenient().when(eventMemoryRepository.findByCategory(anySet()))
                .thenReturn(List.of(e1, e2));

        List<Event> result = eventService.findByDynamicCondition(request);

        assertEquals(2, result.size());
        assertThat(result).extracting(Event::getEventId).containsExactlyInAnyOrder(1L, 2L);
    }
}