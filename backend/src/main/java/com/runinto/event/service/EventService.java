package com.runinto.event.service;

import com.runinto.chat.domain.repository.chatroom.Chatroom;
import com.runinto.chat.domain.repository.chatroom.ChatroomH2Repository;
import com.runinto.chat.domain.repository.chatroom.ChatroomParticipant;
import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventParticipant;
import com.runinto.event.domain.ParticipationStatus;
import com.runinto.event.domain.repository.EventH2Repository;
import com.runinto.event.dto.request.FindEventRequest;
import com.runinto.exception.event.EventNotFoundException;
import com.runinto.exception.event.PermissionDeniedException;
import com.runinto.exception.user.UserIdNotFoundException;
import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserH2Repository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventService {

    private final UserH2Repository userH2Repository;
    private final EventH2Repository eventRepository;
    private final ChatroomH2Repository chatroomH2Repository;

    public EventService(final EventH2Repository eventRepository, final UserH2Repository userH2Repository, ChatroomH2Repository chatroomH2Repository) {
        this.eventRepository = eventRepository;
        this.userH2Repository = userH2Repository;
        this.chatroomH2Repository = chatroomH2Repository;
    }

    public Event findById(long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."));
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

    //이벤트와 채팅방은 1:1 관계 이벤트에 채팅방이 종속관계이므로 채팅방 관리는 이벤트의 생명주기를 따르고 관리함
    @Transactional
    public Event createEventWithChatroom(Event event, User user) {

        if(!userH2Repository.existsByUserId(user.getUserId())) {
            throw new UserIdNotFoundException("User id not found: " + user.getUserId() + " .");
        }

        event.setHost(user);

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

    @Transactional
    public boolean delete(long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        return eventRepository.delete(event); // 연관된 엔티티들 모두 cascade 삭제됨
    }

    @Transactional
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

        event.getEventParticipants().add(participant);
        user.getEventParticipants().add(participant);
    }

}
