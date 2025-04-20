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
@RequiredArgsConstructor
public class EventService {
    @Autowired
    private final EventRepositoryImple eventMemoryRepository;

    public Optional<Event> findById(long id) {
        return eventMemoryRepository.findById(id);
    }

    public List<Event> findAll() {
        return eventMemoryRepository.findAll();
    }

    public void save(Event event) {
        eventMemoryRepository.save(event);
    }

    public List<Event> findByDynamicCondition(FindEventRequest request) {
        return eventMemoryRepository.findAll().stream()
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
        return eventMemoryRepository.delete(id);
    }

    public void clear() {eventMemoryRepository.clear();}
}
