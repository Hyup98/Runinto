package com.runinto.event.service;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;
import com.runinto.event.domain.repository.EventH2Repository;
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
    private EventH2Repository eventH2Repository;

    @InjectMocks
    private EventService eventService;


    private Event createMockEvent(Long id, EventType type, double lat, double lng) {
        Event event = Event.builder()
                .eventId(id)
                .title("이벤트 " + id + " - " + type.name())
                .description("설명 " + id)
                .latitude(lat)
                .longitude(lng)
                .maxParticipants(10)
                .participants(0)
                .chatroomId(id)
                .creationTime(Time.valueOf(LocalTime.now()))
                .build();

        EventCategory category = EventCategory.builder()
                .category(type)
                .event(event)
                .build();

        event.setEventCategories(Set.of(category));
        return event;
    }

    @Test
    @DisplayName("위치만 일치하고 카테고리는 불일치 → 결과 없음")
    void locationMatchesButCategoryDoesNot() {
        // Given
        Event event = createMockEvent(1L, EventType.TALKING, 37.565, 127.015);
        FindEventRequest request = new FindEventRequest(
                37.567, 127.017, 37.563, 127.013,
                Set.of(EventType.EAT)
        );

        when(eventH2Repository.findByArea(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(event));

        // When
        List<Event> result = eventService.findByDynamicCondition(request);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("카테고리는 일치하지만 위치는 벗어남 → 결과 없음")
    void categoryMatchesButLocationDoesNot() {
        Event event = createMockEvent(2L, EventType.ACTIVITY, 38.000, 128.000); // 범위 밖
        FindEventRequest request = new FindEventRequest(
                37.567, 127.017, 37.563, 127.013,
                Set.of(EventType.ACTIVITY)
        );

        when(eventH2Repository.findByArea(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(event));

        // When
        List<Event> result = eventService.findByDynamicCondition(request);

        // Then
        assertThat(result).isEmpty();
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

        when(eventH2Repository.findByArea(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(e1, e2));

        // When
        List<Event> result = eventService.findByDynamicCondition(request);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Event::getEventId)
                .containsExactlyInAnyOrder(1L, 2L);
    }
}
