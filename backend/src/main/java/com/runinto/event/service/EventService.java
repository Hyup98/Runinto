package com.runinto.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runinto.chat.domain.repository.chatroom.Chatroom;
import com.runinto.chat.domain.repository.chatroom.ChatroomParticipant;
import com.runinto.event.domain.*;
import com.runinto.event.domain.repository.EventParticipantRepository;
import com.runinto.event.domain.repository.EventRepository;
import com.runinto.event.domain.repository.EventSpecifications;
import com.runinto.event.dto.request.CreateEventRequestDto;
import com.runinto.event.dto.request.FindEventRequest;
import com.runinto.event.dto.response.EventResponse;
import com.runinto.exception.event.EventNotFoundException;
import com.runinto.exception.event.PermissionDeniedException;
import com.runinto.exception.user.UserIdNotFoundException;
import common.kafka.dto.CacheUpdateMessage;// common 모듈의 DTO를 import
import com.runinto.kafka.service.KafkaProducerService;
import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserH2Repository;
import com.runinto.util.GeoUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.runinto.event.dto.cache.EventCacheDto;
@Slf4j
@Service
public class EventService {

    private final UserH2Repository userH2Repository;
    private final EventRepository eventRepository;
    private final EventCacheService eventCacheService;
    private final KafkaProducerService kafkaProducerService;
    private final EventParticipantRepository eventParticipantRepository;


    public EventService(final EventRepository eventRepository, final UserH2Repository userH2Repository,
                        EventCacheService eventCacheService, KafkaProducerService kafkaProducerService,
                        EventParticipantRepository eventParticipantRepository) {
        this.eventRepository = eventRepository;
        this.userH2Repository = userH2Repository;
        this.eventCacheService = eventCacheService;
        this.kafkaProducerService = kafkaProducerService;
        this.eventParticipantRepository = eventParticipantRepository;
    }

    public Event findById(long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."));
    }

    public List<Event> findCreatedByUserId(Long userId) {
        return eventRepository.findByHostUserId(userId);
    }

    public void save(Event event) {
        eventRepository.save(event);
    }

    //위치 필터는 필수, 카테고리 필터는 선택
    public List<EventResponse> findByDynamicCondition(FindEventRequest request) {
        boolean hasCategoryFilter = request.getCategories() != null && !request.getCategories().isEmpty();

        List<String> requiredGridIds = GeoUtil.getGridIdsForBoundingBox(request.getSwlatitude(),
                request.getSwlongitude(),
                request.getNelatitude(),
                request.getNelongitude());

        Map<String, List<EventCacheDto>> cachedGrids = eventCacheService.findGridsFromCache(requiredGridIds);

        List<String> missedGridIds = requiredGridIds.stream()
                .filter(id -> !cachedGrids.containsKey(id))
                .collect(Collectors.toList());

        if (!missedGridIds.isEmpty()) {
            Map<String, List<EventCacheDto>> newGrids = eventCacheService.findGridsFromDbAndCache(missedGridIds);
            cachedGrids.putAll(newGrids);
        }

        for (Map.Entry<String, List<EventCacheDto>> entry : cachedGrids.entrySet()) {

            // 현재 항목의 Key (gridId)와 Value (이벤트 목록)를 가져옵니다.
            String gridId = entry.getKey();
            List<EventCacheDto> eventListInGrid = entry.getValue();

            System.out.println("--- Grid ID: " + gridId + " 순회 시작 ---");

            // 2. 안쪽 루프: 해당 Grid에 속한 이벤트 목록(List)을 순회합니다.
            for (EventCacheDto event : eventListInGrid) {

                // 최종적으로 개별 EventCacheDto 객체에 접근하여 원하는 작업을 수행합니다.
                // 예시: 이벤트의 제목을 출력
                System.out.println("이벤트 제목: " + event.title());
                // System.out.println(event.toString()); // 객체 전체 정보 출력
            }

            System.out.println("--- Grid ID: " + gridId + " 순회 종료 ---\n");
        }

        /*Stream<EventCacheDto> eventStream = cachedGrids.values().stream()
                .flatMap(List::stream)
                .distinct();

        eventStream = eventStream.filter(dto ->
                dto.latitude() >= request.getSwlatitude() && dto.latitude() <= request.getNelatitude() &&
                        dto.longitude() >= request.getSwlongitude() && dto.longitude() <= request.getNelongitude());

        if (hasCategoryFilter) {
            eventStream = eventStream.filter(dto ->
                    dto.eventCategories().stream()
                            .anyMatch(ecDto -> request.getCategories().contains(ecDto.getCategory())));
        }

        // 💡 최종적으로 클라이언트에게 보낼 EventResponse로 변환합니다.
        return eventStream
                .map(EventResponse::from) // EventResponse에 EventCacheDto를 받는 from 메소드 필요
                .collect(Collectors.toList());*/

        // --- 2. 단일 스트림 파이프라인으로 필터링 및 변환 ---
        return cachedGrids.values().stream()
                .flatMap(List::stream)
                .distinct()
                .peek(dto -> log.info("### 스트림 시작 (distinct 후): {}", dto.toString())) // peek 1: 스트림 시작점 데이터 확인
                .filter(dto -> {
                    boolean passes = dto.latitude() >= request.getSwlatitude() && dto.latitude() <= request.getNelatitude() &&
                            dto.longitude() >= request.getSwlongitude() && dto.longitude() <= request.getNelongitude();
                    // log.info("위치 필터링 결과: {} -> 통과 여부: {}", dto.title(), passes); // 너무 많은 로그가 찍힐 수 있어 주석 처리
                    return passes;
                })
                .peek(dto -> log.info("### 위치 필터 통과: {}", dto.toString())) // peek 2: 위치 필터를 통과한 데이터 확인
                .filter(dto -> {             // 카테고리 필터링
                    if (!hasCategoryFilter) {
                        return true;
                    }
                    // 이 부분이 수정 포인트입니다.
                    return dto.eventCategories().stream()
                            .anyMatch(ecDto -> {
                                // ecDto.getCategory()는 String 타입이므로, 이를 Enum으로 변환합니다.
                                try {
                                    EventType categoryAsEnum = EventType.valueOf(ecDto.getCategory());
                                    // 변환된 Enum을 사용하여 Set에 포함되어 있는지 확인합니다.
                                    return request.getCategories().contains(categoryAsEnum);
                                } catch (IllegalArgumentException e) {
                                    // DTO에 잘못된 카테고리 문자열이 있는 경우 예외 처리
                                    return false;
                                }
                            });
                })
                .map(EventResponse::from)
                .collect(Collectors.toList());
    }


    public Page<EventResponse> findByDynamicConditionWithPaging(FindEventRequest request, Pageable pageable) {
        // 1. 요청 DTO를 기반으로 Specification 생성
        Specification<Event> spec = createSpecificationFromRequest(request);

        // 2. [DB] 페이징된 ID 목록 조회
        Page<Long> idPage = eventRepository.findIdsWithPaging(spec, pageable);
        List<Long> eventIds = idPage.getContent();

        if (eventIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 3. [Cache] ID 목록으로 캐시에서 데이터 조회
        Map<Long, EventCacheDto> eventDtoMap = eventCacheService.findEventsByIds(eventIds);

        // 4. [DB] 캐시에 없던 ID 목록 조회
        List<Long> missedIds = eventIds.stream()
                .filter(id -> !eventDtoMap.containsKey(id))
                .collect(Collectors.toList());

        if (!missedIds.isEmpty()) {
            List<Event> eventsFromDb = eventRepository.findWithCategoriesByIdIn(missedIds);

            List<EventCacheDto> dtosToCache = new ArrayList<>();
            for (Event event : eventsFromDb) {
                EventCacheDto dto = EventCacheDto.from(event);
                eventDtoMap.put(event.getId(), dto); // 조회 결과 맵에 추가
                dtosToCache.add(dto); // 캐시에 저장할 DTO 목록에 추가
            }

            // 5. [Cache] 새로 조회한 데이터를 캐시에 저장
            eventCacheService.saveEventsToCache(dtosToCache);
        }

        // 6. [App] 최종 페이지 데이터 조립 (ID 순서 유지)
        List<EventResponse> finalContent = eventIds.stream()
                .map(id -> EventResponse.from(eventDtoMap.get(id)))
                .collect(Collectors.toList());

        return new PageImpl<>(finalContent, pageable, idPage.getTotalElements());
    }

    // 💡 Specification 생성 로직을 서비스로 이동
    private Specification<Event> createSpecificationFromRequest(FindEventRequest request) {
        Specification<Event> spec = Specification.where(null); // 항상 참인 조건으로 시작

        if (request.getSwlatitude() != null) {
            spec = spec.and(EventSpecifications.isInArea(
                    request.getSwlatitude(), request.getNelatitude(),
                    request.getSwlongitude(), request.getNelongitude()));
        }
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            spec = spec.and(EventSpecifications.hasCategoryIn(request.getCategories()));
        }

        return spec;
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

        if (!userH2Repository.existsByUserId(user.getUserId())) {
            throw new UserIdNotFoundException("User id not found: " + user.getUserId() + " .");
        }

        event.setHost(user);

        String gridId = GeoUtil.getGridId(event.getLatitude(), event.getLongitude());
        event.setGridId(gridId);

        CacheUpdateMessage message = new CacheUpdateMessage("INVALIDATE_GRID", event.getGridId());
        try {
            kafkaProducerService.send("cache-management-topic", message);
        } catch (Exception e) { // 예외 처리를 좀 더 포괄적으로 변경
            log.error("Kafka 메시지 전송 실패: {}", message, e);
            throw new RuntimeException("Kafka 메시지 전송에 실패했습니다.", e);
        }


        log.info("이벤트 저장 시작");
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

        log.info("채팅방 생성및 연결");
        // 3. 채팅방 생성 및 연결
        Chatroom chatroom = Chatroom.builder()
                .event(savedEvent)
                .build();

        savedEvent.setChatroom(chatroom);
        return savedEvent;
    }

    public void deleteEvent(long eventId, Long currentUserId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("이벤트를 찾을 수 없습니다."));

        // 권한 확인: 현재 사용자가 이벤트의 주최자인지 확인
        if (!event.getHost().getUserId().equals(currentUserId)) {
            throw new PermissionDeniedException("이벤트를 삭제할 권한이 없습니다.");
        }

        String gridId = event.getGridId();
        // 카프카에 캐시 갱신 메시지를 보냅니다.
        boolean isDeleted = eventRepository.delete(event);

        if (isDeleted) {
            CacheUpdateMessage message = new CacheUpdateMessage("INVALIDATE_GRID", gridId);
            kafkaProducerService.send("cache-management-topic", message);
        }
        eventCacheService.invalidateEventCache(eventId);
    }

    public void kickParticipant(Long eventId, Long participantId, Long currentUserId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("이벤트를 찾을 수 없습니다."));

        // 권한 확인: 현재 사용자가 이벤트의 주최자인지 확인
        if (!event.getHost().getUserId().equals(currentUserId)) {
            throw new PermissionDeniedException("참가자를 강퇴할 권한이 없습니다.");
        }

        // 3. 주최자가 자기 자신을 강퇴하는 것을 방지
        if (event.getHost().getUserId().equals(participantId)) {
            throw new IllegalArgumentException("주최자는 자기 자신을 강퇴할 수 없습니다.");
        }

        // 4. 강퇴할 참가자를 이벤트의 참가 목록에서 찾기
        EventParticipant participantToKick = event.getEventParticipants().stream()
                .filter(p -> p.getUser().getUserId().equals(participantId) && p.getStatus() == ParticipationStatus.APPROVED)
                .findFirst()
                .orElseThrow(() -> new UserIdNotFoundException("해당 참가자(APPROVED 상태)를 찾을 수 없습니다."));

        // 💡 새로 만든 Repository 메소드를 호출하여 DB에 직접 업데이트 요청
        eventParticipantRepository.updateStatusById(participantToKick.getId(), ParticipationStatus.REJECTED);
    }

    //public void clear() {eventRepository.clear();}

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
    public void approveParticipant(long eventId, long userId, long apporvingId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found. ID = " + eventId));

        // 권한 확인: 요청자가 이벤트 방장인지 확인
        User eventHost = event.getHost();
        if (eventHost == null || !eventHost.getUserId().equals(userId)) {
            throw new PermissionDeniedException("PermissionDeniedException");
        }

        EventParticipant participant = event.getEventParticipants().stream()
                .filter(ep -> ep.getUser().getUserId().equals(apporvingId))
                .findFirst()
                .orElseThrow(() -> new EventNotFoundException("EventParticipant not found for userId = " + apporvingId));

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
    public void rejectParticipant(long eventId, long userId, long rejectingId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found. ID = " + eventId));

        // 권한 확인: 요청자가 이벤트 방장인지 확인
        User eventHost = event.getHost();
        if (eventHost == null || !eventHost.getUserId().equals(userId)) {
            throw new PermissionDeniedException("PermissionDeniedException");
        }

        EventParticipant participant = event.getEventParticipants().stream()
                .filter(ep -> ep.getUser().getUserId().equals(rejectingId))
                .findFirst()
                .orElseThrow(() -> new EventNotFoundException("EventParticipant not found for userId = " + rejectingId));

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

    public List<EventParticipant> getApprovedEventParticipants(Long eventId) {
        // 1. 이벤트의 존재 여부를 확인합니다.
        eventRepository.findById(eventId); // EventRepository에 existsById가 있다고 가정

        // 2. 새로 만든 Repository 메소드를 사용하여 DB에서 직접 데이터를 조회합니다.
        List<EventParticipant> eventParticipants = eventParticipantRepository.findParticipantsByEventIdAndStatus(eventId, ParticipationStatus.APPROVED);
        return eventParticipants;
    }
}
