package com.runinto.event.presentaion;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;
import com.runinto.event.dto.request.JoinEventRequest;
import com.runinto.event.dto.request.UpdateEventRequest;
import com.runinto.event.dto.response.EventListResponse;
import com.runinto.event.dto.response.EventResponse;
import com.runinto.event.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
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

import java.sql.Time;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

//https://meetup.nhncloud.com/posts/223
@Slf4j
@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventService eventService;

    @LocalServerPort
    private int port;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/events";

        if (eventService.findAll().isEmpty()) {
            for (int i = 1; i <= 10; i++) {
                EventType type = (i % 2 == 0) ? EventType.TALKING : EventType.ACTIVITY;
                double lat = 37.50 + (i * 0.001);
                double lng = 127.00 + (i * 0.001);

                Event event = Event.builder()
                        .title("이벤트 " + i)
                        .description("설명 " + i)
                        .maxParticipants(10)
                        .creationTime(Time.valueOf(LocalTime.now()))
                        .latitude(lat)
                        .longitude(lng)
                        .participants(0)
                        .build();

                EventCategory category = EventCategory.builder()
                        .category(type)
                        .build();
                category.setEvent(event);

                Set<EventCategory> categories = new HashSet<>();
                categories.add(category);
                event.setEventCategories(categories);

                eventService.save(event);
            }
        }
    }

    @AfterEach
    void tearDown() {
        eventService.clear();
    }

    @Test
    @DisplayName("카테고리 및 위치 필터로 이벤트 목록 조회")
    void testGetAllEventsWithQueryParams() {
        // given
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
        assertThat(response.getBody().getEvents()).isNotNull();
        assertThat(response.getBody().getEvents().size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("이벤트 업데이트 테스트")
    void testUpdateEvent() {
        // given
        UpdateEventRequest request = new UpdateEventRequest(
                "새 타이틀",
                "새 설명",
                20,
                37.8,
                127.5,
                true
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateEventRequest> entity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<EventResponse> response = restTemplate.exchange(
                baseUrl + "/1",
                HttpMethod.PATCH,
                entity,
                EventResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("새 타이틀");
    }

    @Test
    @DisplayName("이벤트 참여 요청 테스트")
    void testAddParticipant() {
        // given
        JoinEventRequest request = new JoinEventRequest(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JoinEventRequest> entity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/1/participants",
                entity,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("성공적으로 참여");
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 참여 요청 테스트")
    void testAddParticipantinNon() {
        // given
        JoinEventRequest request = new JoinEventRequest(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JoinEventRequest> entity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/11/participants",
                entity,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("이벤트 삭제 테스트")
    void testDeleteEvent() {
        // when
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/1",
                HttpMethod.DELETE,
                null,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("deleted");
        assertThat(eventService.findAll().size()).isEqualTo(9);
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 삭제 요청 테스트")
    void testDeleteNonEvent() {
        // when
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/0",
                HttpMethod.DELETE,
                null,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(eventService.findAll().size()).isEqualTo(10);
    }
}
