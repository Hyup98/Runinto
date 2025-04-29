package com.runinto.event.service;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventType;
import com.runinto.event.domain.repository.EventRepositoryImple;
import com.runinto.event.dto.request.FindEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EventService {

    private final EventRepositoryImple eventRepository;

    public EventService(final EventRepositoryImple eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Optional<Event> findById(long id) {
        return eventRepository.findById(id);
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public void save(Event event) {
        eventRepository.save(event);
    }

    public List<Event> findByDynamicCondition(FindEventRequest request) {
        return eventRepository.findAll().stream()
                .filter(event -> {
                    if (request.getSwlatitude() != null && request.getSwlongitude() != null &&
                            request.getNelatitude() != null && request.getNelongitude() != null) {
                        return event.isInArea(
                                request.getNelatitude(),
                                request.getNelongitude(),
                                request.getSwlatitude(),
                                request.getSwlongitude()
                        );
                    }
                    return true;
                })
                .filter(event -> {
                    if (request.getCategories() != null && !request.getCategories().isEmpty()) {
                        return event.hasMatchingCategory(request.getCategories());
                    }
                    return true;
                })
                .toList();
    }

    public boolean delete(long id) {
        return eventRepository.delete(id);
    }

    public void clear() {eventRepository.clear();}
}
