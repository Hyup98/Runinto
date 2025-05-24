package com.runinto.event.presentaion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runinto.auth.dto.request.LoginRequest; // Ensure this path is correct
import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;
import com.runinto.event.domain.EventParticipant;
import com.runinto.event.domain.ParticipationStatus;
import com.runinto.event.domain.repository.EventJpaRepository;
import com.runinto.event.dto.request.JoinEventRequest;
import com.runinto.event.dto.request.UpdateEventRequest;
import com.runinto.event.dto.response.EventListResponse;
import com.runinto.event.dto.response.EventResponse;
import com.runinto.event.service.EventService;
import com.runinto.user.domain.Gender;
import com.runinto.user.domain.Role;
import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserJpaRepository;
import com.runinto.user.dto.response.EventParticipantsResponse;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional; // For LazyInitializationException

import java.sql.Time;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Mockito/ByteBuddy Warnings:
 * The warnings like "Mockito is currently self-attaching..." are related to your build configuration
 * and future JDK compatibility. They don't directly cause test failures but should be addressed
 * by configuring the Mockito agent in your build.gradle or pom.xml as per Mockito documentation.
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventJpaRepository eventJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private EventService eventService; // For programmatic setup

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private String baseEventUrl;
    private String baseAuthUrl;

    private User eventCreator;
    private User regularUser1;
    private User regularUser2;

    private final String creatorPassword = "Password123!";
    private final String regularUser1Password = "Password456!";
    private final String regularUser2Password = "Password789!";

    private HttpHeaders creatorHeaders;
    private HttpHeaders regularUser1Headers;
    private HttpHeaders regularUser2Headers;
    private HttpHeaders unauthenticatedHeaders;

    private Event mainTestEvent;

    // Simplified request body for POST /events, matching @RequestBody Event
    static class MinimalEventRequestBody {
        public String title;
        public String description;
        public Integer maxParticipants;
        public Double latitude;
        public Double longitude;
        public Boolean isPublic;
        public Set<EventType> categories;
        public Time creationTime;
        // Add other fields that your Event entity might require for basic creation via JSON
        // Ensure Event entity has a constructor or setters that Jackson can use with these fields.

        public MinimalEventRequestBody() {} // Default constructor for Jackson

        public MinimalEventRequestBody(String title, String description, int maxParticipants, double lat, double lon, boolean isPublic, Set<EventType> categories) {
            this.title = title;
            this.description = description;
            this.maxParticipants = maxParticipants;
            this.latitude = lat;
            this.longitude = lon;
            this.isPublic = isPublic;
            this.categories = categories != null ? categories : new HashSet<>();
            this.creationTime = Time.valueOf(LocalTime.now());
        }
    }

    @BeforeEach
    void setUp() {
        // Clean up before each test. Order can be important.
        // Using deleteAll() instead of deleteAllInBatch() to ensure JPA cascades are triggered.
        // This is crucial for DataIntegrityViolationException if Event has cascaded children like Chatroom.
        eventJpaRepository.deleteAll();
        userJpaRepository.deleteAll(); // Assuming users are recreated or managed per test or globally if @TestInstance(PER_CLASS)

        baseEventUrl = "http://localhost:" + port + "/events";
        baseAuthUrl = "http://localhost:" + port + "/auth";
        unauthenticatedHeaders = new HttpHeaders();

        // Create users
        eventCreator = User.builder()
                .name("이벤트생성자").email("creator-" + UUID.randomUUID() + "@example.com").password(creatorPassword)
                .role(Role.USER).gender(Gender.MALE).age(30)
                .description("Event creator desc").imgUrl("http://example.com/creator.png")
                .eventParticipants(new HashSet<>()).chatParticipations(new HashSet<>()) // Initialize collections
                .build();
        userJpaRepository.saveAndFlush(eventCreator); // saveAndFlush to ensure ID is generated and persisted before auth

        regularUser1 = User.builder()
                .name("일반사용자1").email("regular1-" + UUID.randomUUID() + "@example.com").password(regularUser1Password)
                .role(Role.USER).gender(Gender.FEMALE).age(25)
                .description("Regular user 1 desc").imgUrl("http://example.com/regular1.png")
                .eventParticipants(new HashSet<>()).chatParticipations(new HashSet<>())
                .build();
        userJpaRepository.saveAndFlush(regularUser1);

        regularUser2 = User.builder()
                .name("일반사용자2").email("regular2-" + UUID.randomUUID() + "@example.com").password(regularUser2Password)
                .role(Role.USER).gender(Gender.MALE).age(28)
                .description("Regular user 2 desc").imgUrl("http://example.com/regular2.png")
                .eventParticipants(new HashSet<>()).chatParticipations(new HashSet<>())
                .build();
        userJpaRepository.saveAndFlush(regularUser2);

        // Authenticate users
        creatorHeaders = authenticateAndGetHeaders(eventCreator.getEmail(), creatorPassword);
        regularUser1Headers = authenticateAndGetHeaders(regularUser1.getEmail(), regularUser1Password);
        regularUser2Headers = authenticateAndGetHeaders(regularUser2.getEmail(), regularUser2Password);

        // Create mainTestEvent for general use in tests
        MinimalEventRequestBody createPayload = new MinimalEventRequestBody(
                "메인 테스트 이벤트", "통합 테스트용 기본 이벤트입니다.", 10,
                37.50123, 127.00123, true, Set.of(EventType.ACTIVITY)
        );
        HttpEntity<MinimalEventRequestBody> createRequestEntity = new HttpEntity<>(createPayload, creatorHeaders);
        String createUrl = baseEventUrl + "?user=" + eventCreator.getUserId(); // Attempting to satisfy @RequestParam User user
        ResponseEntity<EventResponse> createResponse = null;

        try {
            createResponse = restTemplate.postForEntity(createUrl, createRequestEntity, EventResponse.class);
        } catch (Exception e) {
            log.warn("API call for event creation failed in setUp (URL: {}). Error: {}. This often indicates an issue with the '@RequestParam User user' signature in EventController.createEventV2 OR an HttpMessageConverter issue if the error is UnknownContentTypeException. Falling back to programmatic creation.", createUrl, e.getMessage(), e);
            if (e instanceof org.springframework.web.client.RestClientException) {
                try {
                    // Attempt to get the raw response body if RestClientException (which includes UnknownContentTypeException)
                    ResponseEntity<String> rawErrorResponse = restTemplate.postForEntity(createUrl, createRequestEntity, String.class);
                    log.error("Actual server response body during failed event creation: {}", rawErrorResponse.getBody());
                } catch (Exception nestedEx) {
                    log.error("Could not retrieve raw error response body.", nestedEx);
                }
            }
        }

        if (createResponse != null && createResponse.getStatusCode() == HttpStatus.CREATED && createResponse.getBody() != null) {
            Long mainEventId = createResponse.getBody().getEventId();
            mainTestEvent = eventJpaRepository.findById(mainEventId).orElseThrow(
                    () -> new IllegalStateException("Failed to fetch mainTestEvent from DB after API creation. ID: " + mainEventId)
            );
        } else {
            log.info("Using programmatic event creation for mainTestEvent (User: {}) due to API call issue.", eventCreator.getEmail());
            Event tempEvent = Event.builder()
                    .title("메인 테스트 이벤트 (Programmatic)").description("통합 테스트용 기본 이벤트입니다.")
                    .maxParticipants(10).creationTime(Time.valueOf(LocalTime.now()))
                    .latitude(37.50123).longitude(127.00123)
                    .categories(new HashSet<>()).participants(new HashSet<>())
                    .build();
            if (createPayload.categories != null && !createPayload.categories.isEmpty()) {
                Set<EventCategory> cats = createPayload.categories.stream()
                        .map(et -> EventCategory.builder().category(et).event(tempEvent).build())
                        .collect(Collectors.toSet());
                tempEvent.setEventCategories(cats);
            }
            mainTestEvent = eventService.createEventWithChatroom(tempEvent, eventCreator); // Assumes eventCreator is persisted
        }
        assertThat(mainTestEvent).as("mainTestEvent should be initialized in setUp").isNotNull();
        assertThat(mainTestEvent.getId()).as("mainTestEvent ID should not be null").isNotNull();
    }

    @AfterEach
    void tearDown() {
        // Clean up all data after each test to ensure isolation
        // Using deleteAll() to trigger JPA cascades for dependent entities (like Chatroom for Event)
        eventJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    private HttpHeaders authenticateAndGetHeaders(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        HttpEntity<LoginRequest> requestEntity = new HttpEntity<>(loginRequest);
        ResponseEntity<String> response = restTemplate.postForEntity(baseAuthUrl + "/signin", requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            HttpHeaders headers = new HttpHeaders();
            if (cookies != null && !cookies.isEmpty()) {
                headers.add(HttpHeaders.COOKIE, cookies.get(0).split(";", 2)[0]);
            }
            return headers;
        }
        Assertions.fail("Authentication failed for user: " + email + ". Status: " + response.getStatusCode() + " Body: " + response.getBody());
        return new HttpHeaders(); // Should not reach here
    }


    // --- Test Nests ---

    @Nested
    @DisplayName("GET /events/{event_id} (이벤트 단건 조회)")
    class GetEventById {
        @Test
        @DisplayName("성공: 인증된 사용자가 공개 이벤트를 ID로 조회한다")
        void success_getPublicEventById_authenticated() {
            HttpEntity<String> entity = new HttpEntity<>(null, regularUser1Headers);
            ResponseEntity<EventResponse> response = null;
            String rawResponse = "N/A";
            try {
                response = restTemplate.exchange(
                        baseEventUrl + "/" + mainTestEvent.getId(), HttpMethod.GET, entity, EventResponse.class);
            } catch (org.springframework.web.client.UnknownContentTypeException e) {
                rawResponse = restTemplate.exchange(baseEventUrl + "/" + mainTestEvent.getId(), HttpMethod.GET, entity, String.class).getBody();
                log.error("UnknownContentTypeException for GET /events/{}: {}\nResponse body was: {}", mainTestEvent.getId(), e.getMessage(), rawResponse, e);
                Assertions.fail("Failed to deserialize EventResponse. Check DTO and actual server response.", e);
            } catch (Exception e) {
                Assertions.fail("Unexpected error during GET /events/" + mainTestEvent.getId(), e);
            }

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            EventResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getEventId()).isEqualTo(mainTestEvent.getId());
            assertThat(body.getTitle()).isEqualTo(mainTestEvent.getTitle());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이벤트 ID로 조회 시 404를 반환한다")
        void failure_getEventById_notFound() {
            HttpEntity<String> entity = new HttpEntity<>(null, creatorHeaders);
            ResponseEntity<String> response = restTemplate.exchange(
                    baseEventUrl + "/99999", HttpMethod.GET, entity, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST /events (이벤트 생성)")
    class CreateEvent {
        @Test
        @DisplayName("성공 시도: 인증된 사용자가 새 이벤트 생성 (주의: @RequestParam User user 이슈)")
        void attempt_createEvent_authenticated() {
            MinimalEventRequestBody createPayload = new MinimalEventRequestBody(
                    "API 생성 이벤트", "설명.", 5, 37.505, 127.005, false, Set.of(EventType.TALKING)
            );
            HttpEntity<MinimalEventRequestBody> entity = new HttpEntity<>(createPayload, regularUser1Headers);
            String createUrl = baseEventUrl + "?user=" + regularUser1.getUserId();
            ResponseEntity<EventResponse> response = null;

            try {
                response = restTemplate.postForEntity(createUrl, entity, EventResponse.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                EventResponse body = response.getBody();
                assertThat(body).isNotNull();
                assertThat(body.getTitle()).isEqualTo(createPayload.title);
                // Further DB checks...
            } catch (org.springframework.web.client.RestClientException e) {
                log.error("Test 'attempt_createEvent_authenticated' API call failed. Error: {}", e.getMessage(), e);
                String actualBody = "N/A";
                try {
                    ResponseEntity<String> errorResponse = restTemplate.postForEntity(createUrl, entity, String.class);
                    actualBody = errorResponse.getBody();
                    log.error("Actual error response body for createEvent: {}", actualBody);
                } catch (Exception logEx) {
                    log.error("Could not retrieve error response body for createEvent.", logEx);
                }
                Assertions.fail("API call failed for event creation. Problematic signature: EventController.createEventV2(@RequestBody Event, @RequestParam User). Or server returned unexpected content. Actual response: " + actualBody, e);
            }
        }
    }

    @Nested
    @DisplayName("PATCH /events/{event_id} (이벤트 수정)")
    class UpdateEvent {
        @Test
        @Transactional // For DB checks accessing lazy collections of updatedInDb if any
        @DisplayName("성공: 이벤트 매니저가 자신의 이벤트를 수정한다")
        void success_updateEvent_byManager() {
            // Ensure eventCreator is manager of mainTestEvent (which should be true from setup)
            assertTrue(mainTestEvent.getEventParticipants().stream()
                    .anyMatch(ep -> ep.getUser().getUserId().equals(eventCreator.getUserId()) && ep.getStatus() == ParticipationStatus.MANAGER));

            UpdateEventRequest updateDto = new UpdateEventRequest("수정 제목", "수정 설명", 15, 37.5055, 127.0055, false);
            HttpEntity<UpdateEventRequest> entity = new HttpEntity<>(updateDto, creatorHeaders);
            ResponseEntity<EventResponse> response = null;
            String rawResponse = "N/A";

            try {
                response = restTemplate.exchange(baseEventUrl + "/" + mainTestEvent.getId(), HttpMethod.PATCH, entity, EventResponse.class);
            } catch (org.springframework.web.client.UnknownContentTypeException e) {
                rawResponse = restTemplate.exchange(baseEventUrl + "/" + mainTestEvent.getId(), HttpMethod.PATCH, entity, String.class).getBody();
                log.error("UnknownContentTypeException for PATCH /events/{}: {}\nResponse body was: {}", mainTestEvent.getId(), e.getMessage(), rawResponse, e);
                Assertions.fail("Failed to deserialize EventResponse for update. Check DTO and actual server response.", e);
            }  catch (Exception e) {
                Assertions.fail("Unexpected error during PATCH /events/" + mainTestEvent.getId(), e);
            }

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            EventResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getTitle()).isEqualTo(updateDto.getTitle());
            Event updatedInDb = eventJpaRepository.findById(mainTestEvent.getId()).orElseThrow();
            assertThat(updatedInDb.getTitle()).isEqualTo(updateDto.getTitle());
        }
    }

    @Nested
    @DisplayName("GET /events (이벤트 목록 조회 및 필터링)")
    class GetAllEvents {
        Event eventActivityNear;

        @BeforeEach
        void setUpEventsForFilterTests() {
            // Note: main @BeforeEach already cleans events. This ensures only events for this context exist.
            eventJpaRepository.deleteAll();
            eventActivityNear = createTestEventInSetup("가까운 활동", eventCreator, EventType.ACTIVITY, true, 37.502, 127.002, 10);
            // Re-create mainTestEvent here if its properties are critical for these specific filters
            // and to avoid state interference from the outer mainTestEvent.
            mainTestEvent = createTestEventInSetup("메인 필터 이벤트", eventCreator, EventType.ACTIVITY, true, 37.50123, 127.00123, 10);
        }

        private Event createTestEventInSetup(String title, User creator, EventType type, boolean isPublic, double lat, double lon, int max) {
            Event event = Event.builder().title(title).description("D").maxParticipants(max)
                    .creationTime(Time.valueOf(LocalTime.now())).latitude(lat).longitude(lon)
                    .categories(new HashSet<>()).participants(new HashSet<>()).build();
            if (type != null) {
                EventCategory category = EventCategory.builder().category(type).event(event).build();
                event.getEventCategories().add(category);
            }
            return eventService.createEventWithChatroom(event, creator);
        }

        @Test
        @DisplayName("성공: 위치 및 공개 ACTIVITY 카테고리 필터링")
        void success_getFilteredEvents_LocationAndPublicCategory() {
            // Ensure the 'isPublic' parameter is actually used by your controller's GetAllEventsV1
            // and EventService.findByDynamicCondition if you expect strict filtering by it.
            String url = String.format("%s?swLat=%.3f&neLat=%.3f&swLng=%.3f&neLng=%.3f&category=%s&isPublic=true",
                    baseEventUrl, 37.500, 37.503, 127.000, 127.003, EventType.ACTIVITY.name());
            HttpEntity<String> entity = new HttpEntity<>(null, regularUser1Headers);
            ResponseEntity<EventListResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, EventListResponse.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getEvents()).extracting(Event::getTitle)
                    .containsExactlyInAnyOrder(mainTestEvent.getTitle(), eventActivityNear.getTitle());
        }
    }

    @Nested
    @DisplayName("DELETE /events/{event_id} (이벤트 삭제)")
    class DeleteEvent {
        @Test
        // @Transactional // Not strictly needed here unless checking lazy post-delete states of other entities
        @DisplayName("성공: 이벤트 매니저가 자신의 이벤트를 삭제한다")
        void success_deleteEvent_byManager() {
            // Ensure eventCreator is manager of mainTestEvent (which should be true from setup)
            assertTrue(mainTestEvent.getEventParticipants().stream()
                    .anyMatch(ep -> ep.getUser().getUserId().equals(eventCreator.getUserId()) && ep.getStatus() == ParticipationStatus.MANAGER));

            HttpEntity<String> entity = new HttpEntity<>(null, creatorHeaders);
            ResponseEntity<String> response = restTemplate.exchange(baseEventUrl + "/" + mainTestEvent.getId(), HttpMethod.DELETE, entity, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("Event deleted.");
            assertThat(eventJpaRepository.findById(mainTestEvent.getId())).isNotPresent();
        }
    }

    @Nested
    @DisplayName("Event Participation (/events/{event_id}/participants)")
    class EventParticipation {
        private Event joinableEvent;

        @BeforeEach
        void setUpParticipationTests() {
            // Create a fresh event for these participation tests
            Event eventData = Event.builder().title("참여 이벤트").description("신청").maxParticipants(3)
                    .creationTime(Time.valueOf(LocalTime.now())).latitude(37.555).longitude(127.055)
                    .categories(new HashSet<>()).participants(new HashSet<>()).build();
            EventCategory gameCategory = EventCategory.builder().category(EventType.GAME).event(eventData).build();
            eventData.getEventCategories().add(gameCategory); // Ensure category is part of the event for persistence
            joinableEvent = eventService.createEventWithChatroom(eventData, eventCreator);
        }

        @Test
        @Transactional // For lazy loading eventInDb.getEventParticipants()
        @DisplayName("POST /participants: 성공 - 사용자 참여 요청")
        void success_applyToEvent() {
            HttpEntity<JoinEventRequest> entity = new HttpEntity<>(new JoinEventRequest(regularUser1.getUserId()), regularUser1Headers);
            ResponseEntity<String> response = restTemplate.postForEntity(baseEventUrl + "/" + joinableEvent.getId() + "/participants", entity, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            Event eventInDb = eventJpaRepository.findById(joinableEvent.getId()).orElseThrow();
            assertTrue(eventInDb.getEventParticipants().stream() // Requires active session
                    .anyMatch(ep -> ep.getUser().getUserId().equals(regularUser1.getUserId()) && ep.getStatus() == ParticipationStatus.REQUESTED));
        }

        @Test
        @Transactional // For ObjectMapper access and potential lazy loading if service method relies on it
        @DisplayName("GET /participants/requested: 성공 - 매니저가 참여 요청 목록 조회")
        void success_getRequestedParticipants_byManager() throws Exception {
            // regularUser1 applies
            restTemplate.postForEntity(baseEventUrl + "/" + joinableEvent.getId() + "/participants", new HttpEntity<>(new JoinEventRequest(regularUser1.getUserId()), regularUser1Headers), String.class);

            HttpEntity<String> entity = new HttpEntity<>(null, creatorHeaders); // eventCreator is manager
            ResponseEntity<String> responseEntity = restTemplate.exchange(baseEventUrl + "/" + joinableEvent.getId() + "/participants/requested", HttpMethod.GET, entity, String.class);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<EventParticipantsResponse> responses = objectMapper.readValue(responseEntity.getBody(), new TypeReference<>() {});
            assertThat(responses).hasSize(1).extracting(EventParticipantsResponse::getUserId).contains(regularUser1.getUserId());
        }

        @Test
        @Transactional // For lazy loading eventInDb.getEventParticipants() and eventInDb.getChatroom().getParticipants()
        @DisplayName("POST /{eventId}/participants/{userId}/approve: 성공 - 매니저가 참여 승인")
        void success_approveParticipant_byManager() {
            restTemplate.postForEntity(baseEventUrl + "/" + joinableEvent.getId() + "/participants", new HttpEntity<>(new JoinEventRequest(regularUser1.getUserId()), regularUser1Headers), String.class);

            HttpEntity<Void> approveEntity = new HttpEntity<>(null, creatorHeaders);
            ResponseEntity<Void> response = restTemplate.postForEntity(baseEventUrl + "/" + joinableEvent.getId() + "/participants/" + regularUser1.getUserId() + "/approve", approveEntity, Void.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            Event eventInDb = eventJpaRepository.findById(joinableEvent.getId()).orElseThrow();
            EventParticipant participant = eventInDb.getEventParticipants().stream() // Requires active session
                    .filter(ep -> ep.getUser().getUserId().equals(regularUser1.getUserId())).findFirst().orElseThrow();
            assertThat(participant.getStatus()).isEqualTo(ParticipationStatus.APPROVED);
            assertTrue(eventInDb.getChatroom().getParticipants().stream() // Requires active session
                    .anyMatch(cp -> cp.getUser().getUserId().equals(regularUser1.getUserId())));
        }

        @Test
        @Transactional // For lazy loading eventInDb.getEventParticipants()
        @DisplayName("POST /{eventId}/participants/{userId}/reject: 성공 - 매니저가 참여 거절")
        void success_rejectParticipant_byManager() {
            restTemplate.postForEntity(baseEventUrl + "/" + joinableEvent.getId() + "/participants", new HttpEntity<>(new JoinEventRequest(regularUser1.getUserId()), regularUser1Headers), String.class);

            HttpEntity<Void> rejectEntity = new HttpEntity<>(null, creatorHeaders);
            ResponseEntity<Void> response = restTemplate.postForEntity(baseEventUrl + "/" + joinableEvent.getId() + "/participants/" + regularUser1.getUserId() + "/reject", rejectEntity, Void.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            Event eventInDb = eventJpaRepository.findById(joinableEvent.getId()).orElseThrow();
            EventParticipant participant = eventInDb.getEventParticipants().stream() // Requires active session
                    .filter(ep -> ep.getUser().getUserId().equals(regularUser1.getUserId())).findFirst().orElseThrow();
            assertThat(participant.getStatus()).isEqualTo(ParticipationStatus.REJECTED);
        }
    }
}