package com.runinto.event.service;

import com.runinto.chat.domain.repository.chatroom.Chatroom;
import com.runinto.event.domain.Event;
import com.runinto.event.domain.repository.EventH2Repository;
import com.runinto.event.domain.repository.EventRepositoryImple;
import com.runinto.event.dto.request.FindEventRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EventService {

    private final EventRepositoryImple eventRepository;

    public EventService(final EventH2Repository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Optional<Event> findById(long id) {
        return eventRepository.findById(id);
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Transactional
    public void save(Event event) {
        eventRepository.save(event);
    }

    //todo 지금 이건 모든 이벤트를 다 가져온 후 필터링을 거는 방식 -> db에서 가져올 때 sql로 필터링을 하는 방법으로 바꿔야 한다.
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

    @Transactional
    public Event createEventWithChatroom(Event event) {
        if (event.getChatroom() == null) {
            Chatroom chatroom = Chatroom.builder()
                    .event(event)
                    .messages(new ArrayList<>())
                    .participants(new HashSet<>())
                    .build();
            event.setChatroom(chatroom);
        }

        eventRepository.save(event);
        return event;
    }


    @Transactional
    public boolean delete(long id) {
        return eventRepository.delete(id);
    }

    @Transactional
    public void clear() {eventRepository.clear();}
}
