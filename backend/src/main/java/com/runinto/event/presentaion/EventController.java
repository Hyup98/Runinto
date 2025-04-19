package com.runinto.event.presentaion;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;
import com.runinto.event.dto.request.FindEventRequest;
import com.runinto.event.dto.request.JoinEventRequest;
import com.runinto.event.dto.request.UpdateEventRequest;
import com.runinto.event.dto.response.EventListResponse;
import com.runinto.event.dto.response.EventResponse;
import com.runinto.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("events")
public class EventController {
    @Autowired
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
            @RequestParam double swLat,
            @RequestParam double neLat,
            @RequestParam double swLng,
            @RequestParam double neLng,
            @RequestParam(required = false) Set<EventType> category,
            @RequestParam(required = false) Boolean isPublic
            ) {
        //위도 경도 범위 체크
        if (swLat < -90 || swLat > 90 || neLat < -90 || neLat > 90 || neLat < swLat ||
                swLng < -180 || swLng > 180 || neLng < -180 || neLng > 180 || neLng < swLng) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 위도 또는 경도 범위입니다.");
        }
        FindEventRequest condition = new FindEventRequest(swLat, neLat, swLng, neLng, category);
        List<Event> events = eventService.findByDynamicCondition(condition);
        return ResponseEntity.ok(new EventListResponse(events));
    }


    /*이벤트 삭제 요청
    추가예정 기능
    1. 이벤트 관리자만 삭제 가능-> 권한
    2. jwt확인 후 관리자인지 확인
     */
    @DeleteMapping("{event_id}")
    public ResponseEntity<String> DeleteEventV1(@PathVariable("event_id") Long eventId) {
        eventService.delete(eventId);
        return ResponseEntity.ok("Event deleted.");
    }

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
