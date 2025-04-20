package com.runinto.event.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
public class EventMemoryRepository implements EventRepositoryImple{

    private Map<Long, Event> events = new HashMap<>();

    public Optional<Event> findById(long id) {
        return Optional.ofNullable(events.get(id));
    }

    public void save(Event event) {
        events.put(event.getEventId(), event);
        log.info("Saved event {}, MapSizes : {}", event, events.size());
    }

    public List<Event> findAll() {
        return new ArrayList<>(events.values());
    }

    public List<Event> findByCategory(Set<EventType> categories) {
        return events.values().stream()
                .filter(event -> event.hasMatchingCategory(categories))
                .toList();
    }

    public List<Event> findByArea(double nelatitude, double nelongitude, double swlatitude, double swlongitude) {
        return events.values().stream()
                .filter(event -> event.isInArea(nelatitude, nelongitude, swlatitude, swlongitude))
                .toList();
    }

    public int getSize(){
        return events.size();
    }

    public boolean delete(long id) {
        return events.remove(id)!= null;
    }

    public void clear() {events.clear();}
}
