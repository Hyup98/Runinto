package com.runinto.event.presentaion;

import com.runinto.event.domain.Event;
import com.runinto.event.dto.request.FindEventRequest;
import com.runinto.event.dto.request.JoinEventRequest;
import com.runinto.event.dto.request.UpdateEventRequest;
import com.runinto.event.dto.response.EventListResponse;
import com.runinto.event.dto.response.EventResponse;
import com.runinto.event.service.EventService;
import com.sun.jdi.request.EventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("events")
public class EventController {
    private final EventService eventService;

    @GetMapping("{event_id}")
    public ResponseEntity<EventResponse> GetEventV1(@PathVariable("event_id") Long eventId) {
        Event event = eventService.findById(eventId).orElseThrow();
        final EventResponse eventResponse = EventResponse.from(event);
        return ResponseEntity.ok(eventResponse);
    }

    @PatchMapping("{event_id}")
    public ResponseEntity<EventResponse> UpdateEventV1(
            @PathVariable("event_id") Long eventId,
            @RequestBody UpdateEventRequest eventRequest) {
        Event event = eventService.findById(eventId).orElseThrow();

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
            @RequestBody FindEventRequest findEventRequest
            ) {
        List<Event> events = eventService.findByDynamicCondition(findEventRequest);

        EventListResponse result = new EventListResponse(events);
        return ResponseEntity.ok(result);
    }


    //이벤트 삭제 요청

    //이벤트 참여 요청 -> 채팅 서버를 따로 빼서 관리
    @PostMapping("{event_id}/participants")
    public ResponseEntity<String> AddParticipantV1(
            @PathVariable("event_id") Long eventId,
            @RequestBody JoinEventRequest joinEventRequest) {

        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."));

        if (event.getParticipants() >= event.getMaxParticipants()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("참여 인원이 가득 찼습니다.");
        }

        event.application(joinEventRequest.getUserId());
        return ResponseEntity.ok("이벤트에 성공적으로 참여했습니다.");
    }




}
