package com.runinto.event.presentaion;

import com.runinto.auth.domain.SessionConst;
import com.runinto.auth.domain.UserSessionDto;
import com.runinto.chat.service.ChatService;
import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventParticipant;
import com.runinto.event.domain.EventType;
import com.runinto.event.domain.ParticipationStatus;
import com.runinto.event.dto.request.CreateEventRequestDto;
import com.runinto.event.dto.request.FindEventRequest;
import com.runinto.event.dto.request.UpdateEventRequest;
import com.runinto.event.dto.response.EventListResponse;
import com.runinto.event.dto.response.EventResponse;
import com.runinto.event.service.EventService;
import com.runinto.user.domain.User;
import com.runinto.user.dto.response.EventParticipantsResponse;
import com.runinto.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    public EventController(final EventService eventService, final UserService userService) {
        this.userService = userService;
        this.eventService = eventService;
    }

    @GetMapping("{event_id}")
    public ResponseEntity<EventResponse> GetEventV1(@PathVariable("event_id") Long eventId) {
        Event event = eventService.findById(eventId);
        final EventResponse eventResponse = EventResponse.from(event);
        return ResponseEntity.ok(eventResponse);
    }

    //채팅방도 함께 생성해주는 이벤트 생성함수
    @PostMapping
    public ResponseEntity<EventResponse> createEventV2(
            @RequestBody CreateEventRequestDto eventRequestDto,
            @RequestParam("user") Long userId) {
        log.info("event create 호출");

        User eventCreator = userService.findById(userId); // (3) ID로 User 객체 조회

        Event event = eventService.createEventFromDto(eventRequestDto);

        Event saved = eventService.createEventWithChatroom(event, eventCreator);
        log.info("event & 채팅방 create!!");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EventResponse.from(saved));
    }

    @PatchMapping("{event_id}")
    public ResponseEntity<EventResponse> UpdateEventV1(
            @PathVariable("event_id") Long eventId,
            @RequestBody UpdateEventRequest eventRequest) {
        Event event = eventService.findById(eventId);
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

    //그리드 기반 캐싱을 이용한 검색 함수
    /*@GetMapping()
    public ResponseEntity<EventListResponse> GetAllEventsV1(
            @RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double swLat,
            @RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double neLat,
            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double swLng,
            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double neLng,
            @RequestParam(required = false) Set<EventType> category
            // @RequestParam(required = false) Boolean isPublic
    ) {
        if (swLat != null && swLng != null && neLat != null && neLng != null) {
             if (neLat < swLat || neLng < swLng) {
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 범위입니다.");
             }
         }
        log.info("탐색할 범위 ->  swLat : " + String.valueOf(swLat) + "    neLat : " + String.valueOf(neLat) +"swLng : " + String.valueOf(swLng) + "    neLng : " + String.valueOf(neLng));
        if (category != null) {
            category.forEach(eventType -> log.info("이벤트 타입 : {}", eventType));
        }
        else {
            log.info("카테코리 없음");
        }


        FindEventRequest condition = FindEventRequest.builder()
                .swlatitude(swLat)
                .nelatitude(neLat)
                .swlongitude(swLng)
                .nelongitude(neLng)
                .categories(category)
                // .isPublic(isPublic) // isPublic이 FindEventRequest에 설정되지 않음
                .build();

        List<EventResponse> events = eventService.findByDynamicCondition(condition);
        log.info(String.valueOf(events.size()));
        return ResponseEntity.ok(new EventListResponse(events));
    }*/

    @GetMapping()
    public ResponseEntity<Page<EventResponse>> GetAllEventsV2(
            @RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double swLat,
            @RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double neLat,
            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double swLng,
            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double neLng,
            @RequestParam(required = false) Set<EventType> category,
            @PageableDefault(size = 10, sort = "creationTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if (swLat != null && swLng != null && neLat != null && neLng != null) {
            if (neLat < swLat || neLng < swLng) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 범위입니다.");
            }
        }
        log.info("탐색할 범위 ->  swLat : " + String.valueOf(swLat) + "    neLat : " + String.valueOf(neLat) +"swLng : " + String.valueOf(swLng) + "    neLng : " + String.valueOf(neLng));
        if (category != null) {
            category.forEach(eventType -> log.info("이벤트 타입 : {}", eventType));
        }
        else {
            log.info("카테코리 없음");
        }

        FindEventRequest condition = FindEventRequest.builder()
                .swlatitude(swLat).nelatitude(neLat)
                .swlongitude(swLng).nelongitude(neLng)
                .categories(category)
                .build();

        // 💡 페이징 지원 서비스 메소드 호출
        Page<EventResponse> eventsPage = eventService.findByDynamicConditionWithPaging(condition, pageable);

        log.info("총 {} 페이지 중 {} 페이지 조회, 총 {}개 이벤트", eventsPage.getTotalPages(), eventsPage.getNumber(), eventsPage.getTotalElements());
        return ResponseEntity.ok(eventsPage);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long eventId,
            HttpSession session) {

        // 1. 세션에서 현재 로그인한 사용자 정보 가져오기
        UserSessionDto sessionUser = (UserSessionDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (sessionUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. 서비스 계층에 이벤트 ID와 사용자 ID를 넘겨 삭제 위임 (권한 확인은 서비스에서 처리)
        eventService.deleteEvent(eventId, sessionUser.getUserId());

        return ResponseEntity.noContent().build(); // 성공 시 204 No Content 응답
    }

    @DeleteMapping("/{eventId}/participants/{participantId}")
    public ResponseEntity<Void> kickParticipant(
            @PathVariable Long eventId,
            @PathVariable Long participantId,
            HttpSession session) {

        UserSessionDto sessionUser = (UserSessionDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (sessionUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        eventService.kickParticipant(eventId, participantId, sessionUser.getUserId());

        return ResponseEntity.noContent().build();
    }

    //이벤트 참여 요청 -> 채팅 서버를 따로 빼서 관리
    @PostMapping("{event_id}/participants")
    public ResponseEntity<String> AddParticipantV1(
            @PathVariable("event_id") Long eventId,
            @RequestParam("user") Long userId ) {

        eventService.appliyToEvent(eventId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{eventId}/participants/{userId}/approve")
    public ResponseEntity<Void> approveParticipant(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false); // 현재 세션 가져오기 (없으면 null 반환)

        if (session == null) {
            log.warn("세션이 없습니다. 인증되지 않은 사용자의 접근 시도.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserSessionDto sessionDto = (UserSessionDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        eventService.approveParticipant(eventId, sessionDto.getUserId(), userId);

        return ResponseEntity.ok().build();
    }



    @PostMapping("/{eventId}/participants/{userId}/reject")
    public ResponseEntity<Void> rejectParticipant(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false); // 현재 세션 가져오기 (없으면 null 반환)

        if (session == null) {
            log.warn("세션이 없습니다. 인증되지 않은 사용자의 접근 시도.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserSessionDto sessionDto = (UserSessionDto) session.getAttribute(SessionConst.LOGIN_MEMBER);


        eventService.rejectParticipant(eventId, sessionDto.getUserId(), userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{eventId}/participants/{status}")
    public ResponseEntity<List<EventParticipantsResponse>> getApprovedEventParticipants(@PathVariable Long eventId,  @PathVariable ParticipationStatus status) {

        List<EventParticipant> requested = new ArrayList<>();

        if(status.equals(ParticipationStatus.APPROVED)) {
             requested = eventService.getApprovedEventParticipants(eventId);
        }
        else if(status.equals(ParticipationStatus.REQUESTED)) {
            requested = eventService.getEventParticipants(eventId);
        }

        List<EventParticipantsResponse> responses = requested.stream()
                .map(EventParticipantsResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
