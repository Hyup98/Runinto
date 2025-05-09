package com.runinto.event.presentaion;

import com.runinto.chat.domain.repository.chatroom.Chatroom;
import com.runinto.chat.presntation.ChatController;
import com.runinto.chat.service.ChatService;
import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;
import com.runinto.event.dto.request.FindEventRequest;
import com.runinto.event.dto.request.JoinEventRequest;
import com.runinto.event.dto.request.UpdateEventRequest;
import com.runinto.event.dto.response.EventListResponse;
import com.runinto.event.dto.response.EventResponse;
import com.runinto.event.service.EventService;
import com.runinto.user.domain.User;
import com.runinto.user.service.UserService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Validated
@RestController
@RequestMapping("events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final ChatService chatService;

    public EventController(final EventService eventService, final UserService userService, ChatService chatService) {
        this.userService = userService;
        this.eventService = eventService;
        this.chatService = chatService;
    }

    @GetMapping("{event_id}")
    public ResponseEntity<EventResponse> GetEventV1(@PathVariable("event_id") Long eventId) {
        Event event = eventService.findById(eventId).orElseThrow();
        final EventResponse eventResponse = EventResponse.from(event);
        return ResponseEntity.ok(eventResponse);
    }

    //채팅방도 함께 생성해주는 이벤트 생성함수
    @PostMapping
    public ResponseEntity<EventResponse> createEventV2(@RequestBody Event event) {
        log.info("Creating event: {}", event.getTitle());

        Event saved = eventService.createEventWithChatroom(event);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EventResponse.from(saved));
    }

    @GetMapping("/test")
    public ResponseEntity<EventResponse> getTempEvent() {

        Event testEvent = Event.builder()
                .title("테스트 이벤트")
                .description("이벤트 설명입니다.")
                .maxParticipants(10)
                .latitude(37.5665)
                .longitude(126.9780)
                //.chatroom(1L)
                .participants(0)
                .build();

        EventCategory category = EventCategory.builder()
                .category(EventType.MOVIE)
                .build();

        category.setEvent(testEvent);
        // Set을 생성하여 카테고리를 추가
        Set<EventCategory> categories = new HashSet<>();
        categories.add(category);
        testEvent.setEventCategories(categories); // Event 객체에 카테고리 설정

        // 이벤트 저장 시 Event와 연관된 EventCategory도 함께 저장됩니다 (CascadeType.ALL 설정 시).
        eventService.save(testEvent);
        return ResponseEntity.ok(EventResponse.from(testEvent));
    }

    @PatchMapping("{event_id}")
    public ResponseEntity<EventResponse> UpdateEventV1(
            @PathVariable("event_id") Long eventId,
            @RequestBody UpdateEventRequest eventRequest) {
        Event event = eventService.findById(eventId).orElse(null);
        if(event == null) {
            return ResponseEntity.notFound().build();
        }

        if (eventRequest.getTitle() != null) event.setTitle(eventRequest.getTitle());
        if (eventRequest.getDescription() != null) event.setDescription(eventRequest.getDescription());
        if (eventRequest.getMaxParticipants() != null) event.setMaxParticipants(eventRequest.getMaxParticipants());
        if (eventRequest.getLatitude() != null) event.setLatitude(eventRequest.getLatitude());
        if (eventRequest.getLongitude() != null) event.setLongitude(eventRequest.getLongitude());
        if (eventRequest.getIsPublic() != null) event.setPublic(eventRequest.getIsPublic());

        eventService.save(event);

        EventResponse eventResponse = EventResponse.from(event);
        return ResponseEntity.ok(eventResponse);
    }

    /*v1
    카테고리, 위치만 필터링 하는 함수
     */
    @GetMapping()
    public ResponseEntity<EventListResponse> GetAllEventsV1(
            @RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double swLat,
            @RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double neLat,
            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double swLng,
            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double neLng,
            @RequestParam(required = false) Set<EventType> category,
            @RequestParam(required = false) Boolean isPublic
            ) {
        //위도 경도 범위 체크
        if (swLat != null && swLng != null && neLat != null && neLng != null) {
            if (neLat < swLat || neLng < swLng) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 범위입니다.");
            }
        }

        FindEventRequest condition = FindEventRequest.builder()
                .swlatitude(swLat)
                .nelatitude(neLat)
                .swlongitude(swLng)
                .nelongitude(neLng)
                .categories(category)
                .build();

        List<Event> events = eventService.findByDynamicCondition(condition);
        for (Event event : events) {
            log.info("\n " + event.toString());
        }
        return ResponseEntity.ok(new EventListResponse(events));
    }


    /*이벤트 삭제 요청
    추가예정 기능
    1. 이벤트 관리자만 삭제 가능-> 권한
    2. jwt확인 후 관리자인지 확인
     */
    @DeleteMapping("{event_id}")
    public ResponseEntity<String> DeleteEventV1(@PathVariable("event_id") Long eventId) {
        if(eventService.delete(eventId))
            return ResponseEntity.ok("Event deleted.");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    //이벤트 참여 요청 -> 채팅 서버를 따로 빼서 관리
    //              -> 일단 무조건 채팅도 같이 참여되게 설정 -> 종속관계
    @PostMapping("{event_id}/participants")
    public ResponseEntity<String> AddParticipantV1(
            @PathVariable("event_id") Long eventId,
            @RequestBody JoinEventRequest joinEventRequest) {

        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."));

        log.info("event.getParticipants() : " + event.getParticipants() + ",  event.getMaxParticipants() : " + event.getMaxParticipants());

        if (event.getParticipants() >= event.getMaxParticipants()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("참여 인원이 가득 찼습니다.");
        }
        User user = userService.findById(joinEventRequest.getUserId());

        event.application(user); // 실제 참여 추가
        event.setParticipants(event.getParticipants() + 1);
        eventService.save(event); // 저장

        Chatroom chatroom = chatService.getChatroomByEventId(eventId);
        chatroom.addParticipant(user);
        chatService.save(chatroom);

        return ResponseEntity.ok("이벤트에 성공적으로 참여했습니다.");
    }


}
