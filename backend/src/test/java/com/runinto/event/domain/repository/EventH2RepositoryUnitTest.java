package com.runinto.event.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import java.sql.Time;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
/*

@DataJpaTest
@Import(EventH2Repository.class)
class EventH2RepositoryUnitTest {

    @Autowired
    private EventH2Repository eventH2Repository;

    @Autowired
    private EventJpaRepository eventJpaRepository;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        eventJpaRepository.deleteAll();

        // Create test events
        for (long i = 1; i <= 10; i++) {
            Event event = Event.builder()
                    .title("이벤트 " + i)
                    .description("설명입니다 " + i)
                    .maxParticipants(10)
                    .creationTime(Time.valueOf(LocalTime.now()))
                    .latitude(37.56 + (i * 0.001))
                    .longitude(127.01 + (i * 0.001))
                    .participants((int) (i % 5))
                    .build();

            // Create and associate category
            EventCategory category = EventCategory.builder()
                    .category(EventType.ACTIVITY)
                    .build();
            category.setEvent(event);

            Set<EventCategory> categories = new HashSet<>();
            categories.add(category);
            event.setEventCategories(categories);

            eventH2Repository.save(event);
        }
    }

    @AfterEach
    void tearDown() {
        eventJpaRepository.deleteAll();
    }

    @Test
    void findById() {
        // Given
        Event event = Event.builder()
                .title("이벤트 999")
                .description("설명입니다 999")
                .maxParticipants(10)
                .creationTime(Time.valueOf(LocalTime.now()))
                .latitude(37.56)
                .longitude(127.01)
                .chatroomId(999L)
                .participants(0)
                .build();

        EventCategory category = EventCategory.builder()
                .category(EventType.ACTIVITY)
                .event(event)
                .build();
        event.setEventCategories(Set.of(category));

        eventH2Repository.save(event);

        // When
        Long id = event.getId();
        Optional<Event> eventOptional = eventH2Repository.findById(id);

        // Then
        assertTrue(eventOptional.isPresent());
        assertEquals("이벤트 999", eventOptional.get().getTitle());
    }

    @Test
    void save() {
        // Given
        Event event = Event.builder()
                .title("새 이벤트")
                .description("새 이벤트 설명")
                .maxParticipants(15)
                .creationTime(Time.valueOf(LocalTime.now()))
                .latitude(37.57)
                .longitude(127.02)
                .chatroomId(11L)
                .participants(0)
                .build();

        EventCategory category = EventCategory.builder()
                .category(EventType.MOVIE)
                .build();
        category.setEvent(event);

        Set<EventCategory> categories = new HashSet<>();
        categories.add(category);
        event.setEventCategories(categories);

        // When
        eventH2Repository.save(event);

        // Then
        assertNotNull(event.getId());
        Optional<Event> savedEvent = eventH2Repository.findById(event.getId());
        assertTrue(savedEvent.isPresent());
        assertEquals("새 이벤트", savedEvent.get().getTitle());
        assertEquals(1, savedEvent.get().getEventCategories().size());
        assertEquals(EventType.MOVIE, savedEvent.get().getEventCategories().iterator().next().getCategory());
    }

    @Test
    void findAll() {
        // Given & When
        List<Event> events = eventH2Repository.findAll();

        // Then
        assertEquals(10, events.size());
    }

    @Test
    void findByCategory() {
        // Given
        Set<EventType> categories = Set.of(EventType.ACTIVITY);
        Set<EventType> nonExistingCategories = Set.of(EventType.EAT);

        // When
        List<Event> events = eventH2Repository.findByCategory(categories);
        List<Event> emptyEvents = eventH2Repository.findByCategory(nonExistingCategories);

        // Then
        assertEquals(10, events.size());
        assertEquals(0, emptyEvents.size());
    }

    @Test
    void findByArea() {
        // Given
        double swLat = 37.562;
        double swLng = 127.012;
        double neLat = 37.566;
        double neLng = 127.016;

        // When
        List<Event> result = eventH2Repository.findByArea(neLat, neLng, swLat, swLng);

        // Then
        assertFalse(result.isEmpty());

        for (Event event : result) {
            assertTrue(event.getLatitude() >= swLat && event.getLatitude() <= neLat);
            assertTrue(event.getLongitude() >= swLng && event.getLongitude() <= neLng);
        }
    }

    @Test
    void delete() {
        // Given
        long eventId = 1L;
        assertTrue(eventH2Repository.findById(eventId).isPresent());

        // When
        boolean deleted = eventH2Repository.delete(eventId);

        // Then
        assertTrue(deleted);
        assertFalse(eventH2Repository.findById(eventId).isPresent());
    }
}
*/
