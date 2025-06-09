package com.runinto.event.service;

import com.runinto.chat.domain.repository.chatroom.Chatroom;
import com.runinto.chat.domain.repository.chatroom.ChatroomParticipant;
import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventParticipant;
import com.runinto.event.domain.ParticipationStatus;
import com.runinto.event.domain.repository.EventRepository;
import com.runinto.event.dto.request.CreateEventRequestDto;
import com.runinto.event.dto.request.FindEventRequest;
import com.runinto.exception.event.EventNotFoundException;
import com.runinto.exception.event.PermissionDeniedException;
import com.runinto.exception.user.UserIdNotFoundException;
import com.runinto.kafka.dto.CacheUpdateMessage;
import com.runinto.kafka.service.KafkaProducerService;
import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserH2Repository;
import com.runinto.util.GeoUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class EventService {

    private final UserH2Repository userH2Repository;
    private final EventRepository eventRepository;
    private final EventCacheService eventCacheService;
    private final KafkaProducerService kafkaProducerService;

    public EventService(final EventRepository eventRepository, final UserH2Repository userH2Repository, EventCacheService eventCacheService, KafkaProducerService kafkaProducerService) {
        this.eventRepository = eventRepository;
        this.userH2Repository = userH2Repository;
        this.eventCacheService = eventCacheService;
        this.kafkaProducerService = kafkaProducerService;
    }

    public Event findById(long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."));
    }

    public void save(Event event) {
        eventRepository.save(event);
    }

    //위치 필터는 필수, 카테고리 필터는 선택
    public List<Event> findByDynamicCondition(FindEventRequest request) {
        boolean hasCategoryFilter = request.getCategories() != null && !request.getCategories().isEmpty();

        // 1. 그리드 ID 목록을 계산합니다.
        List<String> requiredGridIds = GeoUtil.getGridIdsForBoundingBox(
                request.getSwlatitude(), request.getSwlongitude(),
                request.getNelatitude(), request.getNelongitude()
        );

        // 2. 캐시에서 그리드 데이터를 먼저 조회합니다.
        Map<String, List<Event>> cachedGrids = eventCacheService.findGridsFromCache(requiredGridIds);
        List<String> missedGridIds = requiredGridIds.stream()
                .filter(id -> !cachedGrids.containsKey(id))
                .collect(Collectors.toList());

        // 3. 캐시에 없었던 그리드(Cache Miss)는 DB에서 조회하여 캐시를 채웁니다.
        if (!missedGridIds.isEmpty()) {
            Map<String, List<Event>> newGrids = eventCacheService.findGridsFromDbAndCache(missedGridIds);
            cachedGrids.putAll(newGrids); // DB 결과를 기존 캐시 결과에 합칩니다.
        }

        // 4. 캐시와 DB에서 가져온 모든 이벤트를 통합합니다.
        List<Event> allEventsInGrids = cachedGrids.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        // 5. 통합된 데이터를 대상으로 최종 필터링을 수행합니다.
        Stream<Event> eventStream = allEventsInGrids.stream();

        // 5-1. 정확한 영역으로 1차 필터링
        eventStream = eventStream.filter(event ->
                event.getLatitude() >= request.getSwlatitude() && event.getLatitude() <= request.getNelatitude() &&
                        event.getLongitude() >= request.getSwlongitude() && event.getLongitude() <= request.getNelongitude());

        // 5-2. 카테고리 필터가 요청에 포함된 경우에만 2차 필터링
        if (hasCategoryFilter) {
            eventStream = eventStream.filter(event ->
                    event.getEventCategories().stream()
                            .anyMatch(ec -> request.getCategories().contains(ec.getCategory())));
        }

        return eventStream.collect(Collectors.toList());
    }


    public Event createEventFromDto(CreateEventRequestDto requestDto) {

        Event event = Event.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .maxParticipants(requestDto.getMaxParticipants())
                .latitude(requestDto.getLatitude())
                .longitude(requestDto.getLongitude())
                .creationTime(requestDto.getCreationTime())
                .participants(new HashSet<>())
                .categories(new HashSet<>())
                .build();
        event.setPublic(requestDto.getIsPublic());

        if (requestDto.getCategories() != null && !requestDto.getCategories().isEmpty()) {
            Set<EventCategory> eventCategories = requestDto.getCategories().stream()
                    .map(eventType -> EventCategory.builder()
                            .category(eventType)
                            .event(event) // 생성된 event 객체를 연결
                            .build())
                    .collect(Collectors.toSet());
            event.setEventCategories(eventCategories);
        }

        return event;
    }

    //이벤트와 채팅방은 1:1 관계 이벤트에 채팅방이 종속관계이므로 채팅방 관리는 이벤트의 생명주기를 따르고 관리함
    @Transactional
    public Event createEventWithChatroom(Event event, User user) {

        if(!userH2Repository.existsByUserId(user.getUserId())) {
            throw new UserIdNotFoundException("User id not found: " + user.getUserId() + " .");
        }

        event.setHost(user);

        String gridId = GeoUtil.getGridId(event.getLatitude(), event.getLongitude());
        event.setGridId(gridId);

        // 캐시 무효화
        //eventCacheService.invalidateGridCache(gridId);

        // 카프카에 캐시 갱신 메시지를 보냅니다.
        CacheUpdateMessage message = new CacheUpdateMessage("INVALIDATE_GRID", gridId);
        kafkaProducerService.send("cache-management-topic", message);

        // 이벤트 저장
        Event savedEvent = eventRepository.save(event);

        // 이벤트 생성자가 방장으로 자동 등록
        EventParticipant eventParticipant = EventParticipant.builder()
                .event(savedEvent)
                .user(user)
                .status(ParticipationStatus.APPROVED)
                .appliedAt(LocalDateTime.now())
                .build();

        savedEvent.getEventParticipants().add(eventParticipant);
        user.getEventParticipants().add(eventParticipant);

        // 3. 채팅방 생성 및 연결
        Chatroom chatroom = Chatroom.builder()
                .event(savedEvent)
                .build();

        savedEvent.setChatroom(chatroom);
        return savedEvent;
    }

    public boolean delete(long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        // 삭제 전 캐시 무효화
        //eventCacheService.invalidateGridCache(event.getGridId());

        // 카프카에 캐시 갱신 메시지를 보냅니다.
        CacheUpdateMessage message = new CacheUpdateMessage("INVALIDATE_GRID", event.getGridId());
        kafkaProducerService.send("cache-management-topic", message);


        return eventRepository.delete(event); // 연관된 엔티티들 모두 cascade 삭제됨
    }

    public void clear() {eventRepository.clear();}

    //채팅방 신청 유저 확인
    public List<EventParticipant> getEventParticipants(long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        return event.getEventParticipants().stream()
                .filter(ep -> ep.getStatus() == ParticipationStatus.REQUESTED)
                .collect(Collectors.toList());
    }

    //이벤트 참여 승인된 유저만 채팅 참여db에 저장
    @Transactional
    public void approveParticipant(long eventId, long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found. ID = " + eventId));

        // 권한 확인: 요청자가 이벤트 방장인지 확인
        User eventHost = event.getHost();
        if (eventHost == null || !eventHost.getUserId().equals(userId)) {
            throw new PermissionDeniedException("PermissionDeniedException");
        }

        EventParticipant participant = event.getEventParticipants().stream()
                .filter(ep -> ep.getUser().getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new EventNotFoundException("EventParticipant not found for userId = " + userId));

        if (participant.getStatus() == ParticipationStatus.APPROVED) {
            throw new IllegalStateException("Participant is already approved.");
        }

        if (participant.getStatus() != ParticipationStatus.REQUESTED) {
            throw new IllegalStateException("Only REQUESTED participants can be approved. Current status: " + participant.getStatus());
        }

        participant.setStatus(ParticipationStatus.APPROVED);

        Chatroom chatroom = event.getChatroom();
        if (chatroom == null) {
            throw new IllegalStateException("No chatroom found for event " + eventId);
        }

        User user = participant.getUser();

        ChatroomParticipant chatParticipant = ChatroomParticipant.builder()
                .chatroom(chatroom)
                .user(user)
                .build();

        chatroom.getParticipants().add(chatParticipant);
        user.getChatParticipations().add(chatParticipant);
    }

    @Transactional
    public void rejectParticipant(long eventId, long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found. ID = " + eventId));

        // 권한 확인: 요청자가 이벤트 방장인지 확인
        User eventHost = event.getHost();
        if (eventHost == null || !eventHost.getUserId().equals(userId)) {
            throw new PermissionDeniedException("PermissionDeniedException");
        }

        EventParticipant participant = event.getEventParticipants().stream()
                .filter(ep -> ep.getUser().getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new EventNotFoundException("EventParticipant not found for userId = " + userId));

        if (participant.getStatus() == ParticipationStatus.REJECTED) {
            throw new IllegalStateException("Participant is already rejected.");
        }

        if (participant.getStatus() != ParticipationStatus.REQUESTED) {
            throw new IllegalStateException("Only REQUESTED participants can be approved. Current status: " + participant.getStatus());
        }

        participant.setStatus(ParticipationStatus.REJECTED);
    }

    @Transactional
    public void appliyToEvent(long eventId, long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found. ID = " + eventId));

        boolean alreadyExists = event.getEventParticipants().stream()
                .anyMatch(ep -> ep.getUser().getUserId().equals(userId));

        if (alreadyExists) {
            throw new IllegalStateException("User already applied or is participating in this event.");
        }

        if (event.getChatroom().getParticipants().size() >= event.getMaxParticipants()) {
            throw new IllegalStateException("이벤트가 다 찼습니다.");
        }

        User user = userH2Repository.findById(userId)
                .orElseThrow(() -> new UserIdNotFoundException("User not found. ID = " + userId));


        EventParticipant participant = EventParticipant.builder()
                .event(event)
                .user(user)
                .status(ParticipationStatus.REQUESTED)
                .appliedAt(LocalDateTime.now())
                .build();

        participant.setEvent(event);
        participant.setUser(user);
        event.getEventParticipants().add(participant);
        user.getEventParticipants().add(participant);
    }
}
