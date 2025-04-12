package com.runinto.event.service;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.repository.EventMemoryRepository;
import com.runinto.event.dto.request.FindEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventMemoryRepository eventMemoryRepository;

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
                .filter(event -> event.isInArea(request.getNelatitude(), request.getNelongitude(), request.getSwlatitude(), request.getSwlongitude()))
                .filter(event -> event.hasMatchingCategory(request.getCategories()))
                .toList();
    }
}
