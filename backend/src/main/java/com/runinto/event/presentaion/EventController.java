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
import java.util.List;
import java.util.Set;

@Slf4j
@Validated
@RestController
@RequestMapping("events")
public class EventController {

    private final EventService eventService;

    public EventController(final EventService eventService) {
        this.eventService = eventService;
    }

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
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") Double swLat,
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") Double neLat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") Double swLng,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") Double neLng,
            @RequestParam(required = false) Set<EventType> category,
            @RequestParam(required = false) Boolean isPublic
            ) {
        //위도 경도 범위 체크
        if (neLat < swLat || neLng < swLng) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 범위입니다.");
        }
        FindEventRequest condition = FindEventRequest.builder()
                .swlatitude(37.50)
                .nelatitude(37.60)
                .swlongitude(127.00)
                .nelongitude(127.10)
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

        return ResponseEntity.ok("이벤트에 성공적으로 참여했습니다.");
    }
}
