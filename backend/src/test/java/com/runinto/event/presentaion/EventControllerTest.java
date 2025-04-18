package com.runinto.event.presentaion;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;
import com.runinto.event.dto.request.JoinEventRequest;
import com.runinto.event.dto.request.UpdateEventRequest;
import com.runinto.event.dto.response.EventListResponse;
import com.runinto.event.dto.response.EventResponse;
import com.runinto.event.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private EventService eventService;

    @LocalServerPort
    private int port;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/events";
    }

    @Test
    @DisplayName("GET /events - 카테고리 및 위치 필터로 이벤트 목록 조회")
    void testGetAllEventsWithQueryParams() {
        // given: 테스트용 이벤트 생성
        List<Event> filteredEvents = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            EventType type = (i % 2 == 0) ? EventType.TALKING : EventType.ACTIVITY;
            double lat = 37.50 + (i * 0.001);  // 37.501 ~ 37.510
            double lng = 127.00 + (i * 0.001); // 127.001 ~ 127.010

            EventCategory category = new EventCategory((long) i, type, (long) i);
            Event event = Event.builder()
                    .eventId((long) i)
                    .title("이벤트 " + i)
                    .description("설명 " + i)
                    .latitude(lat)
                    .longitude(lng)
                    .categories(Set.of(category))
                    .build();

            if (type == EventType.ACTIVITY && lat <= 37.60 && lng <= 127.10) {
                filteredEvents.add(event);
            }
        }

        // mock 서비스가 필터링된 이벤트만 반환하도록 설정
        when(eventService.findByDynamicCondition(any())).thenReturn(filteredEvents);

        String url = baseUrl +
                "?swLat=37.50&neLat=37.60" +
                "&swLng=127.00&neLng=127.10" +
                "&category=ACTIVITY" +
                "&isPublic=true";

        // when
        ResponseEntity<EventListResponse> response =
                restTemplate.getForEntity(url, EventListResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        /*List<Event> result = response.getBody().getEvents();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(filteredEvents.size());

        // 예상 타이틀 목록
        List<String> expectedTitles = filteredEvents.stream()
                .map(Event::getTitle)
                .collect(Collectors.toList());

        // 응답에서 실제 타이틀 목록
        List<String> actualTitles = result.stream()
                .map(Event::getTitle)
                .collect(Collectors.toList());

        // 🔍 디버깅 로그
        System.out.println("expectedTitles = " + expectedTitles);
        System.out.println("actualTitles   = " + actualTitles);*/
    }

    @Test
    @DisplayName("이벤트 업데이트 테스트.")
    void testUpdateEvent() {
        UpdateEventRequest request = new UpdateEventRequest("새 타이틀", "새 설명", 20, 37.8, 127.5, true);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateEventRequest> entity = new HttpEntity<>(request, headers);

        Event updatedEvent = Event.builder().eventId(1L).title("새 타이틀").build();
        when(eventService.findById(1L)).thenReturn(Optional.of(updatedEvent));
        doNothing().when(eventService).save(any());

        ResponseEntity<EventResponse> response = restTemplate.exchange(baseUrl + "/1", HttpMethod.PATCH, entity, EventResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTitle()).isEqualTo("새 타이틀");
    }

    @Test
    @DisplayName("이벤트 참여 요청 테스트.")
    void testAddParticipant() {
        JoinEventRequest request = new JoinEventRequest(101L);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JoinEventRequest> entity = new HttpEntity<>(request, headers);

        Event event = Event.builder().eventId(1L).maxParticipants(10).participants(5).build();
        when(eventService.findById(1L)).thenReturn(Optional.of(event));

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/1/participants", entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("성공적으로 참여");
    }

    @Test
    @DisplayName("이벤트 삭제 테스트.")
    void testDeleteEvent() {
        doNothing().when(eventService).delete(1L);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/1", HttpMethod.DELETE, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("deleted");
    }
}