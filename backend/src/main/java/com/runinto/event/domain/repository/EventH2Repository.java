package com.runinto.event.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@Primary
public class EventH2Repository implements EventRepositoryImple {

    private final EventJpaRepository eventJpaRepository;

    public EventH2Repository(EventJpaRepository eventJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
    }

    public Event findById(long id) {
        return eventJpaRepository.findById(id);
    }

    public Event save(Event event) {
        return eventJpaRepository.save(event);
    }

    public List<Event> findAll() {
        return eventJpaRepository.findAll();
    }

    public List<Event> findByCategory(Set<EventType> categories) {
        return eventJpaRepository.findByCategories(categories);
    }

    public List<Event> findByArea(double nelatitude, double nelongitude, double swlatitude, double swlongitude) {
        return eventJpaRepository.findByArea(nelatitude, nelongitude, swlatitude, swlongitude);
    }

    public int getSize(){
        return (int) eventJpaRepository.count();
    }

    public boolean delete(Event event) {
        if (eventJpaRepository.existsById(event.getId())) {
            eventJpaRepository.deleteById(event.getId());
            return true;
        }
        return false;
    }

    public void clear() {
        eventJpaRepository.deleteAll();
    }
}
