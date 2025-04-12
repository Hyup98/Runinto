package com.runinto.event.domain.repository;

import com.runinto.chat.domain.Chatroom;
import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import lombok.extern.slf4j.Slf4j;

import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class EventMemoryRepository {
    private Map<Long, Event> events;

    public Optional<Event> findById(long id) {
        //return Optional.ofNullable(events.get(id));
        return Optional.of(
                Event.builder()
                        .name("타이틀")                        // 실제로는 title
                        .description("이벤트 입니다!!.")
                        .maxParticipants(5)
                        .creationTime(Time.valueOf(LocalTime.now()))
                        .latitude(37.5665)
                        .longitude(126.9780)
                        .chatroom(new Chatroom(1L))
                        .participants(1)
                        .build()
        );
    }

    public void save(Event event) {
        events.put(event.getEventId(), event);
        log.info("Saved event {}, MapSizes : {}", event, events.size());
    }

    public List<Event> findAll() {
        return new ArrayList<>(events.values());
    }

    public List<Event> findByCategory(List<EventCategory> categories) {
        return events.values().stream()
                .filter(event -> event.hasMatchingCategory(categories))
                .toList();
    }

    public List<Event> findByArea(double nelatitude, double nelongitude, double swlatitude, double swlongitude) {
        return events.values().stream()
                .filter(event -> event.isInArea(nelatitude, nelongitude, swlatitude, swlongitude))
                .toList();
    }

}
