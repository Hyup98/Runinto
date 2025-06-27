package com.runinto.event.presentaion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runinto.auth.dto.request.LoginRequest;
import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;
import com.runinto.event.domain.EventParticipant;
import com.runinto.event.domain.ParticipationStatus;
import com.runinto.event.domain.repository.EventJpaRepository;
import com.runinto.event.dto.request.CreateEventRequestDto;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Time;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager em;

    @LocalServerPort
    private int port;

    private String baseEventUrl;
    private String baseAuthUrl;

    // 테스트 사용자 데이터
    private User eventCreator;
    private User regularUser1;
    private User regularUser2;
    private User adminUser;

    private final String creatorPassword = "Password123!";
    private final String regularUser1Password = "Password456!";
    private final String regularUser2Password = "Password789!";
    private final String adminPassword = "AdminPass123!";

    // 인증 헤더
    private HttpHeaders creatorHeaders;
    private HttpHeaders regularUser1Headers;
    private HttpHeaders regularUser2Headers;
    private HttpHeaders adminHeaders;
    private HttpHeaders unauthenticatedHeaders;

    // 테스트 이벤트 데이터
    private Event mainTestEvent;
    private Event privateEvent;
    private Event fullEvent; // 정원이 찬 이벤트

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
        // 테스트 전 데이터 정리 - 순서가 중요할 수 있음
        // deleteAllInBatch 대신 deleteAll을 사용하여 JPA 캐스케이드가 트리거되도록 함
        // 이는 Event가 Chatroom과 같은 캐스케이드된 자식을 가진 경우 DataIntegrityViolationException을 방지하기 위해 중요
        eventJpaRepository.deleteAll();
        userJpaRepository.deleteAll();

        // 기본 URL 설정
        baseEventUrl = "http://localhost:" + port + "/events";
        baseAuthUrl = "http://localhost:" + port + "/auth";
        unauthenticatedHeaders = new HttpHeaders();

        // 테스트 사용자 생성
        createTestUsers();

        // 사용자 인증 및 헤더 설정
        authenticateUsers();

        // 테스트 이벤트 생성
        createTestEvents();

        // 테스트 데이터 검증
        validateTestSetup();
    }

    /**
     * 테스트에 필요한 사용자 계정 생성
     */
    private void createTestUsers() {
        // 이벤트 생성자 계정
        eventCreator = User.builder()
                .name("이벤트생성자").email("creator-" + UUID.randomUUID() + "@example.com").password(creatorPassword)
                .role(Role.USER).gender(Gender.MALE).age(30)
                .description("Event creator desc").imgUrl("http://example.com/creator.png")
                .eventParticipants(new HashSet<>()).chatParticipations(new HashSet<>())
                .build();
        userJpaRepository.saveAndFlush(eventCreator);

        // 일반 사용자 1
        regularUser1 = User.builder()
                .name("일반사용자1").email("regular1-" + UUID.randomUUID() + "@example.com").password(regularUser1Password)
                .role(Role.USER).gender(Gender.FEMALE).age(25)
                .description("Regular user 1 desc").imgUrl("http://example.com/regular1.png")
                .eventParticipants(new HashSet<>()).chatParticipations(new HashSet<>())
                .build();
        userJpaRepository.saveAndFlush(regularUser1);

        // 일반 사용자 2
        regularUser2 = User.builder()
                .name("일반사용자2").email("regular2-" + UUID.randomUUID() + "@example.com").password(regularUser2Password)
                .role(Role.USER).gender(Gender.MALE).age(28)
                .description("Regular user 2 desc").imgUrl("http://example.com/regular2.png")
                .eventParticipants(new HashSet<>()).chatParticipations(new HashSet<>())
                .build();
        userJpaRepository.saveAndFlush(regularUser2);

        // 관리자 사용자
        adminUser = User.builder()
                .name("관리자").email("admin-" + UUID.randomUUID() + "@example.com").password(adminPassword)
                .role(Role.ADMIN).gender(Gender.MALE).age(35)
                .description("Admin user desc").imgUrl("http://example.com/admin.png")
                .eventParticipants(new HashSet<>()).chatParticipations(new HashSet<>())
                .build();
        userJpaRepository.saveAndFlush(adminUser);
    }

    /**
     * 사용자 인증 및 인증 헤더 설정
     */
    private void authenticateUsers() {
        creatorHeaders = authenticateAndGetHeaders(eventCreator.getEmail(), creatorPassword);
        regularUser1Headers = authenticateAndGetHeaders(regularUser1.getEmail(), regularUser1Password);
        regularUser2Headers = authenticateAndGetHeaders(regularUser2.getEmail(), regularUser2Password);
        adminHeaders = authenticateAndGetHeaders(adminUser.getEmail(), adminPassword);
    }

    /**
     * 테스트에 필요한 이벤트 생성
     */
    private void createTestEvents() {
        // 1. 메인 테스트 이벤트 (공개 이벤트)
        mainTestEvent = createEventProgrammatically(
                "메인 테스트 이벤트", 
                "통합 테스트용 기본 이벤트입니다.", 
                10, 
                37.50123, 127.00123, 
                true, 
                Set.of(EventType.ACTIVITY), 
                eventCreator
        );

        // 2. 비공개 이벤트
        privateEvent = createEventProgrammatically(
                "비공개 테스트 이벤트", 
                "비공개 이벤트 테스트용입니다.", 
                5, 
                37.55123, 127.05123, 
                false, 
                Set.of(EventType.TALKING), 
                eventCreator
        );

        // 3. 정원이 찬 이벤트 (최대 2명, 이미 2명 참여)
        fullEvent = createEventProgrammatically(
                "정원 찬 이벤트", 
                "정원이 찬 이벤트 테스트용입니다.", 
                2, 
                37.60123, 127.10123, 
                true, 
                Set.of(EventType.GAME), 
                eventCreator
        );

        // 정원이 찬 이벤트에 regularUser1을 승인된 참여자로 추가
        try {
            // 참여 신청
            eventService.appliyToEvent(fullEvent.getId(), regularUser1.getUserId());

            // 데이터베이스에서 최신 상태의 이벤트를 다시 로드
            fullEvent = eventService.findById(fullEvent.getId());

            // 참여 승인
            eventService.approveParticipant(fullEvent.getId(), regularUser1.getUserId(),regularUser1.getUserId());

            // 데이터베이스에서 최신 상태의 이벤트를 다시 로드
            fullEvent = eventService.findById(fullEvent.getId());
        } catch (Exception e) {
            log.error("Failed to add regularUser1 to fullEvent: {}", e.getMessage());
        }
    }

    /**
     * 프로그래밍 방식으로 이벤트 생성
     */
    private Event createEventProgrammatically(String title, String description, int maxParticipants, 
                                             double latitude, double longitude, boolean isPublic, 
                                             Set<EventType> categories, User creator) {
        Event event = Event.builder()
                .title(title)
                .description(description)
                .maxParticipants(maxParticipants)
                .creationTime(Time.valueOf(LocalTime.now()))
                .latitude(latitude)
                .longitude(longitude)
                .categories(new HashSet<>())
                .participants(new HashSet<>())
                .build();
        event.setPublic(isPublic);

        if (categories != null && !categories.isEmpty()) {
            Set<EventCategory> cats = categories.stream()
                    .map(et -> EventCategory.builder().category(et).event(event).build())
                    .collect(Collectors.toSet());
            event.setEventCategories(cats);
        }

        return eventService.createEventWithChatroom(event, creator);
    }

    /**
     * 테스트 설정 검증
     */
    private void validateTestSetup() {
        // 메인 테스트 이벤트 검증
        assertThat(mainTestEvent).as("mainTestEvent should be initialized").isNotNull();
        assertThat(mainTestEvent.getId()).as("mainTestEvent ID should not be null").isNotNull();

        // 비공개 이벤트 검증
        assertThat(privateEvent).as("privateEvent should be initialized").isNotNull();
        assertThat(privateEvent.getId()).as("privateEvent ID should not be null").isNotNull();
        assertThat(privateEvent.isPublic()).as("privateEvent should be private").isFalse();

        // 정원이 찬 이벤트 검증
        assertThat(fullEvent).as("fullEvent should be initialized").isNotNull();
        assertThat(fullEvent.getId()).as("fullEvent ID should not be null").isNotNull();

        // 정원이 찬 이벤트에 참여자가 있는지 확인 (creator + regularUser1)
        assertThat(fullEvent.getEventParticipants().size()).as("fullEvent should have 2 participants").isGreaterThanOrEqualTo(2);
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
            // Given
            HttpEntity<String> entity = new HttpEntity<>(null, regularUser1Headers);

            // When
            ResponseEntity<EventResponse> response = null;
            try {
                response = restTemplate.exchange(
                        baseEventUrl + "/" + mainTestEvent.getId(), 
                        HttpMethod.GET, 
                        entity, 
                        EventResponse.class);
            } catch (Exception e) {
                handleApiException(e, "GET", mainTestEvent.getId());
            }

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            EventResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getEventId()).isEqualTo(mainTestEvent.getId());
            assertThat(body.getTitle()).isEqualTo(mainTestEvent.getTitle());
            assertThat(body.getDescription()).isEqualTo(mainTestEvent.getDescription());
            assertThat(body.getMaxParticipants()).isEqualTo(mainTestEvent.getMaxParticipants());
            assertThat(body.getLatitude()).isEqualTo(mainTestEvent.getLatitude());
            assertThat(body.getLongitude()).isEqualTo(mainTestEvent.getLongitude());
        }

        @Test
        @DisplayName("성공: 이벤트 생성자는 자신의 비공개 이벤트를 조회할 수 있다")
        void success_getPrivateEventById_creator() {
            // Given
            HttpEntity<String> entity = new HttpEntity<>(null, creatorHeaders);

            // When
            ResponseEntity<EventResponse> response = null;
            try {
                response = restTemplate.exchange(
                        baseEventUrl + "/" + privateEvent.getId(), 
                        HttpMethod.GET, 
                        entity, 
                        EventResponse.class);
            } catch (Exception e) {
                handleApiException(e, "GET", privateEvent.getId());
            }

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            EventResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getEventId()).isEqualTo(privateEvent.getId());
            assertThat(body.getTitle()).isEqualTo(privateEvent.getTitle());
            assertThat(body.isPublic()).isFalse();
        }

        @Test
        @DisplayName("성공: 관리자는 모든 비공개 이벤트를 조회할 수 있다")
        void success_getPrivateEventById_admin() {
            // Given
            HttpEntity<String> entity = new HttpEntity<>(null, adminHeaders);

            // When
            ResponseEntity<EventResponse> response = null;
            try {
                response = restTemplate.exchange(
                        baseEventUrl + "/" + privateEvent.getId(), 
                        HttpMethod.GET, 
                        entity, 
                        EventResponse.class);
            } catch (Exception e) {
                handleApiException(e, "GET", privateEvent.getId());
            }

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            EventResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getEventId()).isEqualTo(privateEvent.getId());
            assertThat(body.getTitle()).isEqualTo(privateEvent.getTitle());
            assertThat(body.isPublic()).isFalse();
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이벤트 ID로 조회 시 404를 반환한다")
        void failure_getEventById_notFound() {
            // Given
            HttpEntity<String> entity = new HttpEntity<>(null, creatorHeaders);
            long nonExistentId = 99999L;

            // When
            ResponseEntity<String> response = restTemplate.exchange(
                    baseEventUrl + "/" + nonExistentId, 
                    HttpMethod.GET, 
                    entity, 
                    String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * API 예외 처리 헬퍼 메서드
     */
    private void handleApiException(Exception e, String method, Long resourceId) {
        String rawResponse = "N/A";
        if (e instanceof org.springframework.web.client.UnknownContentTypeException) {
            try {
                rawResponse = restTemplate.exchange(
                        baseEventUrl + "/" + resourceId, 
                        HttpMethod.valueOf(method), 
                        new HttpEntity<>(null, regularUser1Headers), 
                        String.class).getBody();
            } catch (Exception ex) {
                log.error("Could not retrieve raw error response", ex);
            }
            log.error("UnknownContentTypeException for {} /events/{}: {}\nResponse body was: {}", 
                    method, resourceId, e.getMessage(), rawResponse, e);
            Assertions.fail("Failed to deserialize response. Check DTO and actual server response.", e);
        } else {
            log.error("Unexpected error during {} /events/{}: {}", method, resourceId, e.getMessage(), e);
            Assertions.fail("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Nested
    @DisplayName("POST /events (이벤트 생성)")
    class CreateEvent {
        @Test
        @DisplayName("성공: 인증된 사용자가 새 이벤트 생성 (공개/비공개)")
        void success_createEvent_authenticated() {
            // Given - 공개 이벤트
            MinimalEventRequestBody createPayload = new MinimalEventRequestBody(
                    "API 생성 이벤트", "설명.", 5, 37.505, 127.005, true, Set.of(EventType.TALKING)
            );
            HttpEntity<MinimalEventRequestBody> entity = new HttpEntity<>(createPayload, regularUser1Headers);
            String createUrl = baseEventUrl + "?user=" + regularUser1.getUserId();

            // When
            ResponseEntity<EventResponse> response = null;
            try {
                response = restTemplate.postForEntity(createUrl, entity, EventResponse.class);
            } catch (org.springframework.web.client.RestClientException e) {
                handleCreateEventException(e, createUrl, entity);
            }

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            EventResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getTitle()).isEqualTo(createPayload.title);
            assertThat(body.getDescription()).isEqualTo(createPayload.description);
            assertThat(body.getMaxParticipants()).isEqualTo(createPayload.maxParticipants);
            assertThat(body.getLatitude()).isEqualTo(createPayload.latitude);
            assertThat(body.getLongitude()).isEqualTo(createPayload.longitude);
            assertThat(body.isPublic()).isEqualTo(createPayload.isPublic);

            // DB 검증
            Event createdEvent = eventJpaRepository.findById(body.getEventId()).orElse(null);
            assertThat(createdEvent).isNotNull();
            assertThat(createdEvent.getTitle()).isEqualTo(createPayload.title);

            // 이벤트 생성자가 매니저로 등록되었는지 확인
            boolean isCreatorManager = createdEvent.getEventParticipants().stream()
                    .anyMatch(ep -> ep.getUser().getUserId().equals(regularUser1.getUserId()) 
                            && ep.getStatus() == ParticipationStatus.MANAGER);
            assertThat(isCreatorManager).isTrue();

            // 채팅방이 생성되었는지 확인
            assertThat(createdEvent.getChatroom()).isNotNull();

            // Given - 비공개 이벤트
            MinimalEventRequestBody privatePayload = new MinimalEventRequestBody(
                    "비공개 API 이벤트", "비공개 이벤트 설명", 3, 37.510, 127.010, false, Set.of(EventType.GAME)
            );
            HttpEntity<MinimalEventRequestBody> privateEntity = new HttpEntity<>(privatePayload, regularUser1Headers);

            // When
            ResponseEntity<EventResponse> privateResponse = null;
            try {
                privateResponse = restTemplate.postForEntity(createUrl, privateEntity, EventResponse.class);
            } catch (org.springframework.web.client.RestClientException e) {
                handleCreateEventException(e, createUrl, privateEntity);
            }

            // Then
            assertThat(privateResponse).isNotNull();
            assertThat(privateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            EventResponse privateBody = privateResponse.getBody();
            assertThat(privateBody).isNotNull();
            assertThat(privateBody.isPublic()).isFalse();

            // DB 검증
            Event privateEvent = eventJpaRepository.findById(privateBody.getEventId()).orElse(null);
            assertThat(privateEvent).isNotNull();
            assertThat(privateEvent.isPublic()).isFalse();
        }

        @Test
        @DisplayName("성공: 여러 카테고리를 가진 이벤트 생성")
        void success_createEventWithMultipleCategories() {
            // Given
            Set<EventType> multipleCategories = Set.of(EventType.ACTIVITY, EventType.TALKING, EventType.GAME);

            // CreateEventRequestDto를 사용하여 페이로드 생성
            CreateEventRequestDto createPayload = CreateEventRequestDto.builder()
                    .title("다중 카테고리 이벤트")
                    .description("여러 카테고리를 가진 이벤트")
                    .maxParticipants(8)
                    .latitude(37.515)
                    .longitude(127.015)
                    .isPublic(true)
                    .categories(multipleCategories)
                    .creationTime(Time.valueOf(LocalTime.now())) // MinimalEventRequestBody와 동일하게 현재 시간 설정
                    .build();

            HttpHeaders postHeaders = new HttpHeaders();
            postHeaders.addAll(regularUser1Headers); // 인증 쿠키 복사
            postHeaders.setContentType(MediaType.APPLICATION_JSON); // Content-Type 명시 (서버가 JSON을 기대하므로)

            HttpEntity<CreateEventRequestDto> entity = new HttpEntity<>(createPayload, postHeaders); // HttpEntity 타입을 CreateEventRequestDto로 변경
            String createUrl = baseEventUrl + "?user=" + regularUser1.getUserId();

            // When
            ResponseEntity<EventResponse> response = null;
            try {
                response = restTemplate.postForEntity(createUrl, entity, EventResponse.class);
            } catch (org.springframework.web.client.RestClientException e) {
                // handleCreateEventException 메서드는 HttpEntity<?>를 받으므로 별도 수정 필요 없을 수 있음
                handleCreateEventException(e, createUrl, entity);
            }

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED); // 201 CREATED 기대
            EventResponse body = response.getBody();
            assertThat(body).isNotNull();

            // DB 검증
            Event createdEvent = eventJpaRepository.findById(body.getEventId()).orElse(null);
            assertThat(createdEvent).isNotNull();
            assertThat(createdEvent.getTitle()).isEqualTo(createPayload.getTitle()); // 생성된 이벤트 제목 확인

            // 카테고리 검증
            Set<EventType> eventCategoriesInDb = createdEvent.getEventCategories().stream()
                    .map(EventCategory::getCategory)
                    .collect(Collectors.toSet());
            assertThat(eventCategoriesInDb).containsExactlyInAnyOrderElementsOf(multipleCategories);
        }
        @Test
        @DisplayName("실패: 비인증 사용자의 이벤트 생성 시도")
        void failure_createEvent_unauthenticated() {
            // Given
            MinimalEventRequestBody createPayload = new MinimalEventRequestBody(
                    "비인증 이벤트", "비인증 사용자 이벤트", 5, 37.520, 127.020, true, Set.of(EventType.TALKING)
            );
            HttpEntity<MinimalEventRequestBody> entity = new HttpEntity<>(createPayload, unauthenticatedHeaders);
            String createUrl = baseEventUrl + "?user=" + regularUser1.getUserId();

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(createUrl, entity, String.class);

            // Then
            assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        }

        /**
         * 이벤트 생성 API 예외 처리 헬퍼 메서드
         */
        private void handleCreateEventException(Exception e, String url, HttpEntity<?> entity) {
            log.error("Event creation API call failed. Error: {}", e.getMessage(), e);
            String actualBody = "N/A";
            try {
                ResponseEntity<String> errorResponse = restTemplate.postForEntity(url, entity, String.class);
                actualBody = errorResponse.getBody();
                log.error("Actual error response body for createEvent: {}", actualBody);
            } catch (Exception logEx) {
                log.error("Could not retrieve error response body for createEvent.", logEx);
            }
            Assertions.fail("API call failed for event creation. Problematic signature: EventController.createEventV2(@RequestBody Event, @RequestParam User). Or server returned unexpected content. Actual response: " + actualBody, e);
        }
    }

    @Nested
    @DisplayName("PATCH /events/{event_id} (이벤트 수정)")
    class UpdateEvent {
        @Test
        @Transactional // For DB checks accessing lazy collections of updatedInDb if any
        @DisplayName("성공: 이벤트 매니저가 자신의 이벤트를 수정한다 (전체 및 부분 업데이트) -> 머지 되면 마저 하기")
        void success_updateEvent_byManager() {
            // Given
            // 이벤트 생성자가 매니저인지 확인
            assertTrue(mainTestEvent.getEventParticipants().stream()
                    .anyMatch(ep -> ep.getUser().getUserId().equals(eventCreator.getUserId()) 
                            && ep.getStatus() == ParticipationStatus.MANAGER));

            // 전체 업데이트
            UpdateEventRequest updateDto = new UpdateEventRequest(
                    "수정된 제목", 
                    "수정된 설명", 
                    15, 
                    37.5055, 
                    127.0055,
                    false
            );
            HttpEntity<UpdateEventRequest> entity = new HttpEntity<>(updateDto, creatorHeaders);

            // When - 전체 업데이트
            ResponseEntity<EventResponse> response = null;
            try {
                response = restTemplate.exchange(
                        baseEventUrl + "/" + mainTestEvent.getId(), 
                        HttpMethod.PATCH, 
                        entity, 
                        EventResponse.class);
            } catch (Exception e) {
                handleUpdateEventException(e, mainTestEvent.getId(), entity);
            }

            // Then - 전체 업데이트 검증
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            EventResponse body = response.getBody();
            assertThat(body).isNotNull();

            // 응답 검증
            assertThat(body.getTitle()).isEqualTo(updateDto.getTitle());
            assertThat(body.getDescription()).isEqualTo(updateDto.getDescription());
            assertThat(body.getMaxParticipants()).isEqualTo(updateDto.getMaxParticipants());
            assertThat(body.getLatitude()).isEqualTo(updateDto.getLatitude());
            assertThat(body.getLongitude()).isEqualTo(updateDto.getLongitude());
            assertThat(body.isPublic()).isEqualTo(updateDto.getIsPublic());

            // DB 검증
            Event updatedInDb = eventJpaRepository.findById(mainTestEvent.getId()).orElseThrow();
            assertThat(updatedInDb.getTitle()).isEqualTo(updateDto.getTitle());
            assertThat(updatedInDb.getDescription()).isEqualTo(updateDto.getDescription());
            assertThat(updatedInDb.getMaxParticipants()).isEqualTo(updateDto.getMaxParticipants());
            assertThat(updatedInDb.getLatitude()).isEqualTo(updateDto.getLatitude());
            assertThat(updatedInDb.getLongitude()).isEqualTo(updateDto.getLongitude());
            assertThat(updatedInDb.isPublic()).isEqualTo(updateDto.getIsPublic());

            // Given - 부분 업데이트
            // 제목만 수정하는 부분 업데이트 (다른 필드는 null로 설정)
            UpdateEventRequest partialUpdateDto = new UpdateEventRequest(
                "제목만 수정", 
                null, 
                null, 
                null, 
                null, 
                null
            );

            HttpEntity<UpdateEventRequest> partialEntity = new HttpEntity<>(partialUpdateDto, creatorHeaders);

            // When - 부분 업데이트
            ResponseEntity<EventResponse> partialResponse = null;
            try {
                partialResponse = restTemplate.exchange(
                        baseEventUrl + "/" + mainTestEvent.getId(), 
                        HttpMethod.PATCH, 
                        partialEntity, 
                        EventResponse.class);
            } catch (Exception e) {
                handleUpdateEventException(e, mainTestEvent.getId(), partialEntity);
            }

            // Then - 부분 업데이트 검증
            assertThat(partialResponse).isNotNull();
            assertThat(partialResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            EventResponse partialBody = partialResponse.getBody();
            assertThat(partialBody).isNotNull();

            // 응답 검증 - 제목만 변경되었는지 확인
            assertThat(partialBody.getTitle()).isEqualTo(partialUpdateDto.getTitle());

            // 다른 필드는 이전 업데이트 값과 동일한지 확인
            assertThat(partialBody.getDescription()).isEqualTo(updateDto.getDescription());
            assertThat(partialBody.getMaxParticipants()).isEqualTo(updateDto.getMaxParticipants());

            // DB 검증
            Event partialUpdatedInDb = eventJpaRepository.findById(mainTestEvent.getId()).orElseThrow();
            assertThat(partialUpdatedInDb.getTitle()).isEqualTo(partialUpdateDto.getTitle());
            assertThat(partialUpdatedInDb.getDescription()).isEqualTo(updateDto.getDescription());
        }

        @Test
        @DisplayName("실패: 이벤트 매니저가 아닌 사용자가 이벤트 수정 시도 -> 이것도 나중에")
        void failure_updateEvent_notManager() {
            // Given
            UpdateEventRequest updateDto = new UpdateEventRequest(
                    "무단 수정 시도", 
                    "권한 없는 사용자의 수정 시도", 
                    20, 
                    37.6055, 
                    127.1055, 
                    true
            );

            // regularUser1은 mainTestEvent의 매니저가 아님
            HttpEntity<UpdateEventRequest> entity = new HttpEntity<>(updateDto, regularUser1Headers);

            // When
            ResponseEntity<String> response = restTemplate.exchange(
                    baseEventUrl + "/" + mainTestEvent.getId(), 
                    HttpMethod.PATCH, 
                    entity, 
                    String.class);

            // Then
            // 권한 없음 오류 (403) 또는 다른 4xx 오류가 반환되어야 함
            assertThat(response.getStatusCode().is4xxClientError()).isTrue();

            // DB에서 이벤트가 변경되지 않았는지 확인
            Event unchangedEvent = eventJpaRepository.findById(mainTestEvent.getId()).orElseThrow();
            assertThat(unchangedEvent.getTitle()).isEqualTo(mainTestEvent.getTitle());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이벤트 수정 시도")
        void failure_updateNonExistentEvent() {
            // Given
            UpdateEventRequest updateDto = new UpdateEventRequest(
                    "존재하지 않는 이벤트", 
                    "존재하지 않는 이벤트 수정 시도", 
                    10, 
                    37.5555, 
                    127.5555, 
                    true
            );

            HttpEntity<UpdateEventRequest> entity = new HttpEntity<>(updateDto, creatorHeaders);
            long nonExistentId = 99999L;

            // When
            ResponseEntity<String> response = restTemplate.exchange(
                    baseEventUrl + "/" + nonExistentId, 
                    HttpMethod.PATCH, 
                    entity, 
                    String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        /**
         * 이벤트 수정 API 예외 처리 헬퍼 메서드
         */
        private void handleUpdateEventException(Exception e, Long eventId, HttpEntity<?> entity) {
            String rawResponse = "N/A";
            if (e instanceof org.springframework.web.client.UnknownContentTypeException) {
                try {
                    rawResponse = restTemplate.exchange(
                            baseEventUrl + "/" + eventId, 
                            HttpMethod.PATCH, 
                            entity, 
                            String.class).getBody();
                } catch (Exception ex) {
                    log.error("Could not retrieve raw error response", ex);
                }
                log.error("UnknownContentTypeException for PATCH /events/{}: {}\nResponse body was: {}", 
                        eventId, e.getMessage(), rawResponse, e);
                Assertions.fail("Failed to deserialize EventResponse for update. Check DTO and actual server response.", e);
            } else {
                log.error("Unexpected error during PATCH /events/{}: {}", eventId, e.getMessage(), e);
                Assertions.fail("Unexpected error: " + e.getMessage(), e);
            }
        }
    }

    @Nested
    @DisplayName("GET /events (이벤트 목록 조회 및 필터링)")
    class GetAllEvents {
        // 테스트용 이벤트들
        private Event eventActivityNear;
        private Event eventTalkingNear;
        private Event eventGameFar;
        private Event eventPrivate;

        @BeforeEach
        void setUpEventsForFilterTests() {
            // 기존 이벤트 정리 (테스트 격리를 위해)
            eventJpaRepository.deleteAll();

            // 테스트용 이벤트 생성
            // 1. 가까운 위치의 ACTIVITY 이벤트 (공개)
            eventActivityNear = createTestEventInSetup(
                    "가까운 활동", 
                    "가까운 위치의 활동 이벤트입니다.", 
                    eventCreator, 
                    EventType.ACTIVITY, 
                    true, 
                    37.502, 
                    127.002, 
                    10
            );

            // 2. 가까운 위치의 TALKING 이벤트 (공개)
            eventTalkingNear = createTestEventInSetup(
                    "가까운 대화", 
                    "가까운 위치의 대화 이벤트입니다.", 
                    eventCreator, 
                    EventType.TALKING, 
                    true, 
                    37.501, 
                    127.001, 
                    5
            );

            // 3. 먼 위치의 GAME 이벤트 (공개)
            eventGameFar = createTestEventInSetup(
                    "먼 게임", 
                    "먼 위치의 게임 이벤트입니다.", 
                    eventCreator, 
                    EventType.GAME, 
                    true, 
                    37.600, 
                    127.100, 
                    8
            );

            // 4. 가까운 위치의 비공개 이벤트
            eventPrivate = createTestEventInSetup(
                    "비공개 이벤트", 
                    "가까운 위치의 비공개 이벤트입니다.", 
                    eventCreator, 
                    EventType.ACTIVITY, 
                    false, 
                    37.503, 
                    127.003, 
                    3
            );

            // 5. 메인 테스트 이벤트 재생성 (필터 테스트용)
            mainTestEvent = createTestEventInSetup(
                    "메인 필터 이벤트", 
                    "필터 테스트용 메인 이벤트입니다.", 
                    eventCreator, 
                    EventType.ACTIVITY, 
                    true, 
                    37.50123, 
                    127.00123, 
                    10
            );
        }

        /**
         * 테스트용 이벤트 생성 헬퍼 메서드
         */
        private Event createTestEventInSetup(String title, String description, User creator, 
                                            EventType type, boolean isPublic, 
                                            double lat, double lon, int max) {
            Event event = Event.builder()
                    .title(title)
                    .description(description)
                    .maxParticipants(max)
                    .creationTime(Time.valueOf(LocalTime.now()))
                    .latitude(lat)
                    .longitude(lon)
                    .categories(new HashSet<>())
                    .participants(new HashSet<>())
                    .build();
            event.setPublic(isPublic);

            if (type != null) {
                EventCategory category = EventCategory.builder().category(type).event(event).build();
                event.getEventCategories().add(category);
            }

            return eventService.createEventWithChatroom(event, creator);
        }

        // EventControllerIntegrationTest.java 내의 GetAllEvents 중첩 클래스

        @Test
        @DisplayName("성공: 다양한 필터 조건으로 이벤트 조회 (isPublic 필터링 없음)") // 테스트명 변경 가능
        void success_getFilteredEvents_withVariousConditions() {
            HttpEntity<String> entity = new HttpEntity<>(null, regularUser1Headers);

            // Case 1: 위치 및 ACTIVITY 카테고리 필터링 (isPublic=true는 더 이상 필터링에 영향 없음)
            String url1 = String.format("%s?swLat=%.3f&neLat=%.3f&swLng=%.3f&neLng=%.3f&category=%s&isPublic=true",
                    baseEventUrl, 37.500, 37.503, 127.000, 127.003, EventType.ACTIVITY.name());

            ResponseEntity<EventListResponse> response1 = restTemplate.exchange(
                    url1, HttpMethod.GET, entity, EventListResponse.class);

            assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response1.getBody()).isNotNull();
            // 이제 eventPrivate (비공개, ACTIVITY, 위치 범위 내)도 포함되어야 함
            assertThat(response1.getBody().getEvents()).extracting(EventResponse::getTitle)
                    .containsExactlyInAnyOrder(mainTestEvent.getTitle(), eventActivityNear.getTitle(), eventPrivate.getTitle());
            assertThat(response1.getBody().getEvents()).extracting(EventResponse::getTitle)
                    .doesNotContain(eventTalkingNear.getTitle(), eventGameFar.getTitle());

            // Case 2: 카테고리만으로 필터링 (isPublic 필터링 없음)
            String url2 = String.format("%s?category=%s", baseEventUrl, EventType.TALKING.name());

            ResponseEntity<EventListResponse> response2 = restTemplate.exchange(
                    url2, HttpMethod.GET, entity, EventListResponse.class);

            assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response2.getBody()).isNotNull();
            // eventTalkingNear (공개, TALKING)
            // 만약 비공개 TALKING 이벤트가 있었다면 그것도 포함될 것임. 현재 데이터로는 eventTalkingNear만.
            assertThat(response2.getBody().getEvents()).extracting(EventResponse::getTitle)
                    .containsExactlyInAnyOrder(eventTalkingNear.getTitle());
            assertThat(response2.getBody().getEvents()).extracting(EventResponse::getTitle)
                    .doesNotContain(mainTestEvent.getTitle(), eventActivityNear.getTitle(),
                            eventGameFar.getTitle(), eventPrivate.getTitle());

            // Case 3: 위치만으로 필터링 (isPublic 필터링 없음)
            // 이것이 이전에 실패했던 케이스 ["비공개 이벤트"]가 예상치 못하게 포함되었던 상황입니다.
            // 이제 isPublic 필터링을 안 하므로, "비공개 이벤트"가 포함되는 것이 정상입니다.
            String url3 = String.format("%s?swLat=%.3f&neLat=%.3f&swLng=%.3f&neLng=%.3f",
                    baseEventUrl, 37.500, 37.505, 127.000, 127.005);

            ResponseEntity<EventListResponse> response3 = restTemplate.exchange(
                    url3, HttpMethod.GET, entity, EventListResponse.class);

            assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response3.getBody()).isNotNull();
            // mainTestEvent, eventActivityNear, eventTalkingNear (공개, 위치 범위 내)
            // eventPrivate (비공개, 위치 범위 내)도 이제 포함되어야 함.
            assertThat(response3.getBody().getEvents()).extracting(EventResponse::getTitle)
                    .containsExactlyInAnyOrder(mainTestEvent.getTitle(), eventActivityNear.getTitle(), eventTalkingNear.getTitle(), eventPrivate.getTitle());
            assertThat(response3.getBody().getEvents()).extracting(EventResponse::getTitle)
                    .doesNotContain(eventGameFar.getTitle());

            // Case 4: 필터 없이 모든 이벤트 조회 (isPublic 필터링 없음)
            ResponseEntity<EventListResponse> response4 = restTemplate.exchange(
                    baseEventUrl, HttpMethod.GET, entity, EventListResponse.class);

            assertThat(response4.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response4.getBody()).isNotNull();
            // 모든 이벤트 (mainTestEvent, eventActivityNear, eventTalkingNear, eventGameFar, eventPrivate)가 포함되어야 함
            assertThat(response4.getBody().getEvents()).extracting(EventResponse::getTitle)
                    .containsExactlyInAnyOrder(mainTestEvent.getTitle(), eventActivityNear.getTitle(),
                            eventTalkingNear.getTitle(), eventGameFar.getTitle(), eventPrivate.getTitle());
        }

        @Test
        @DisplayName("실패: 잘못된 위치 범위로 필터링 시도")
        void failure_getFilteredEvents_InvalidLocationRange() {
            // Given - 잘못된 위치 범위 (swLat > neLat, swLng > neLng)
            String url = String.format("%s?swLat=%.3f&neLat=%.3f&swLng=%.3f&neLng=%.3f",
                    baseEventUrl, 37.505, 37.500, 127.005, 127.000);
            HttpEntity<String> entity = new HttpEntity<>(null, regularUser1Headers);

            // When
            ResponseEntity<String> response = restTemplate.exchange(
                    url, 
                    HttpMethod.GET, 
                    entity, 
                    String.class);

            // Then - 잘못된 요청으로 400 Bad Request 응답이 와야 함
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("DELETE /events/{event_id} (이벤트 삭제)")
    class DeleteEvent {
        private Event eventToDelete;

        @BeforeEach
        void setUpDeleteEventTests() {
            // 삭제 테스트용 이벤트 생성
            eventToDelete = createEventProgrammatically(
                    "삭제용 이벤트", 
                    "이벤트 삭제 테스트용 이벤트입니다.", 
                    5, 
                    37.55555, 
                    127.55555, 
                    true, 
                    Set.of(EventType.ACTIVITY), 
                    eventCreator
            );
        }

        @Test
        @DisplayName("이벤트 삭제 성공 및 실패 케이스")
        void testEventDeletion() {
            // Case 1: 이벤트 매니저가 자신의 이벤트를 삭제 (성공)
            // 이벤트 생성자가 매니저인지 확인
            assertTrue(eventToDelete.getEventParticipants().stream()
                    .anyMatch(ep -> ep.getUser().getUserId().equals(eventCreator.getUserId()) 
                            && ep.getStatus() == ParticipationStatus.MANAGER));

            HttpEntity<String> managerEntity = new HttpEntity<>(null, creatorHeaders);
            ResponseEntity<String> managerResponse = restTemplate.exchange(
                    baseEventUrl + "/" + eventToDelete.getId(), 
                    HttpMethod.DELETE, 
                    managerEntity, 
                    String.class);

            assertThat(managerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(managerResponse.getBody()).isEqualTo("Event deleted.");
            assertThat(eventJpaRepository.findById(eventToDelete.getId())).isNotPresent();

            // 새 이벤트 생성 (이전 이벤트가 삭제되었으므로)
            eventToDelete = createEventProgrammatically(
                    "삭제용 이벤트 2", 
                    "이벤트 삭제 테스트용 이벤트입니다.", 
                    5, 
                    37.55555, 
                    127.55555, 
                    true, 
                    Set.of(EventType.ACTIVITY), 
                    eventCreator
            );

            // Case 2: 관리자가 이벤트를 삭제 (성공)
            HttpEntity<String> adminEntity = new HttpEntity<>(null, adminHeaders);
            ResponseEntity<String> adminResponse = restTemplate.exchange(
                    baseEventUrl + "/" + eventToDelete.getId(), 
                    HttpMethod.DELETE, 
                    adminEntity, 
                    String.class);

            assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(eventJpaRepository.findById(eventToDelete.getId())).isNotPresent();

            // 새 이벤트 생성 (이전 이벤트가 삭제되었으므로)
            eventToDelete = createEventProgrammatically(
                    "삭제용 이벤트 3", 
                    "이벤트 삭제 테스트용 이벤트입니다.", 
                    5, 
                    37.55555, 
                    127.55555, 
                    true, 
                    Set.of(EventType.ACTIVITY), 
                    eventCreator
            );

            // Case 3: 이벤트 매니저가 아닌 사용자가 이벤트 삭제 시도 (실패)
            HttpEntity<String> regularUserEntity = new HttpEntity<>(null, regularUser1Headers);
            ResponseEntity<String> unauthorizedResponse = restTemplate.exchange(
                    baseEventUrl + "/" + eventToDelete.getId(), 
                    HttpMethod.DELETE, 
                    regularUserEntity, 
                    String.class);

            assertThat(unauthorizedResponse.getStatusCode().is4xxClientError()).isTrue();
            assertThat(eventJpaRepository.findById(eventToDelete.getId())).isPresent();

            // Case 4: 존재하지 않는 이벤트 삭제 시도 (실패)
            long nonExistentId = 99999L;
            ResponseEntity<String> notFoundResponse = restTemplate.exchange(
                    baseEventUrl + "/" + nonExistentId, 
                    HttpMethod.DELETE, 
                    managerEntity, 
                    String.class);

            assertThat(notFoundResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

            // Case 5: 비인증 사용자의 이벤트 삭제 시도 (실패)
            HttpEntity<String> unauthenticatedEntity = new HttpEntity<>(null, unauthenticatedHeaders);
            ResponseEntity<String> unauthenticatedResponse = restTemplate.exchange(
                    baseEventUrl + "/" + eventToDelete.getId(), 
                    HttpMethod.DELETE, 
                    unauthenticatedEntity, 
                    String.class);

            assertThat(unauthenticatedResponse.getStatusCode().is4xxClientError()).isTrue();
            assertThat(eventJpaRepository.findById(eventToDelete.getId())).isPresent();
        }
    }

    @Nested
    @DisplayName("Event Participation (/events/{event_id}/participants)")
    class EventParticipation {
        private Event joinableEvent;
        private Event fullEvent;

        @BeforeEach
        void setUpParticipationTests() {
            // 1. 참여 가능한 이벤트 생성 (최대 3명)
            joinableEvent = createEventProgrammatically(
                    "참여 이벤트", 
                    "이벤트 참여 테스트용 이벤트입니다.", 
                    3, 
                    37.555, 
                    127.055, 
                    true, 
                    Set.of(EventType.GAME), 
                    eventCreator
            );

            // 2. 정원이 찬 이벤트 생성 (최대 1명, 이미 매니저가 참여 중)
            fullEvent = createEventProgrammatically(
                    "정원 찬 참여 이벤트", 
                    "정원이 찬 이벤트 테스트용입니다.", 
                    1, 
                    37.556, 
                    127.056, 
                    true, 
                    Set.of(EventType.ACTIVITY), 
                    eventCreator
            );
        }

        @Test
        @Transactional // For lazy loading eventInDb.getEventParticipants()
        @DisplayName("이벤트 참여 신청 및 관리 (신청, 조회, 승인, 거절)")
        void testEventParticipationWorkflow() {
            // Step 1: 사용자 참여 신청 (성공 케이스)
            JoinEventRequest joinRequest = new JoinEventRequest(regularUser1.getUserId());
            HttpEntity<JoinEventRequest> entity = new HttpEntity<>(joinRequest, regularUser1Headers);

            ResponseEntity<String> joinResponse = restTemplate.postForEntity(
                    baseEventUrl + "/" + joinableEvent.getId() + "/participants", 
                    entity, 
                    String.class);

            assertThat(joinResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // DB 검증 - 참여 신청 상태 확인
            Event eventInDb = eventJpaRepository.findById(joinableEvent.getId()).orElseThrow();
            boolean userRequested = eventInDb.getEventParticipants().stream()
                    .anyMatch(ep -> ep.getUser().getUserId().equals(regularUser1.getUserId()) 
                            && ep.getStatus() == ParticipationStatus.REQUESTED);
            assertTrue(userRequested, "사용자가 REQUESTED 상태로 이벤트에 참여 신청되어야 합니다");

            // Step 2: 두 번째 사용자도 참여 신청
            JoinEventRequest joinRequest2 = new JoinEventRequest(regularUser2.getUserId());
            HttpEntity<JoinEventRequest> entity2 = new HttpEntity<>(joinRequest2, regularUser2Headers);

            restTemplate.postForEntity(
                    baseEventUrl + "/" + joinableEvent.getId() + "/participants", 
                    entity2, 
                    String.class);

            // Step 3: 매니저가 참여 요청 목록 조회
            HttpEntity<String> managerEntity = new HttpEntity<>(null, creatorHeaders);
            ResponseEntity<String> listResponse = restTemplate.exchange(
                    baseEventUrl + "/" + joinableEvent.getId() + "/participants/requested", 
                    HttpMethod.GET, 
                    managerEntity, 
                    String.class);

            assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<EventParticipantsResponse> responses;
            try {
                responses = objectMapper.readValue(
                        listResponse.getBody(), 
                        new TypeReference<List<EventParticipantsResponse>>() {});
            } catch (Exception e) {
                fail("Failed to parse response: " + e.getMessage());
                return; // This line will never be reached due to fail() above, but needed for compilation
            }

            // 두 사용자의 요청이 모두 목록에 있어야 함
            assertThat(responses).hasSize(2)
                    .extracting(EventParticipantsResponse::getUserId)
                    .containsExactlyInAnyOrder(regularUser1.getUserId(), regularUser2.getUserId());

            // Step 4: 매니저가 첫 번째 사용자 참여 승인
            HttpEntity<Void> approveEntity = new HttpEntity<>(null, creatorHeaders);
            ResponseEntity<Void> approveResponse = restTemplate.postForEntity(
                    baseEventUrl + "/" + joinableEvent.getId() + "/participants/" + regularUser1.getUserId() + "/approve", 
                    approveEntity, 
                    Void.class);

            assertThat(approveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // DB 검증 - 승인 상태 및 채팅방 참여 확인
            eventInDb = eventJpaRepository.findById(joinableEvent.getId()).orElseThrow();
            EventParticipant approvedParticipant = eventInDb.getEventParticipants().stream()
                    .filter(ep -> ep.getUser().getUserId().equals(regularUser1.getUserId()))
                    .findFirst()
                    .orElseThrow();
            assertThat(approvedParticipant.getStatus()).isEqualTo(ParticipationStatus.APPROVED);

            boolean userInChatroom = eventInDb.getChatroom().getParticipants().stream()
                    .anyMatch(cp -> cp.getUser().getUserId().equals(regularUser1.getUserId()));
            assertTrue(userInChatroom, "승인된 사용자는 채팅방에 추가되어야 합니다");

            // Step 5: 매니저가 두 번째 사용자 참여 거절
            HttpEntity<Void> rejectEntity = new HttpEntity<>(null, creatorHeaders);
            ResponseEntity<Void> rejectResponse = restTemplate.postForEntity(
                    baseEventUrl + "/" + joinableEvent.getId() + "/participants/" + regularUser2.getUserId() + "/reject", 
                    rejectEntity, 
                    Void.class);

            assertThat(rejectResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // DB 검증 - 거절 상태 및 채팅방 미참여 확인
            eventInDb = eventJpaRepository.findById(joinableEvent.getId()).orElseThrow();
            EventParticipant rejectedParticipant = eventInDb.getEventParticipants().stream()
                    .filter(ep -> ep.getUser().getUserId().equals(regularUser2.getUserId()))
                    .findFirst()
                    .orElseThrow();
            assertThat(rejectedParticipant.getStatus()).isEqualTo(ParticipationStatus.REJECTED);

            boolean rejectedUserInChatroom = eventInDb.getChatroom().getParticipants().stream()
                    .anyMatch(cp -> cp.getUser().getUserId().equals(regularUser2.getUserId()));
            assertFalse(rejectedUserInChatroom, "거절된 사용자는 채팅방에 추가되지 않아야 합니다");
        }

        @Test
        @DisplayName("이벤트 참여 신청 실패 케이스 (중복 신청, 정원 초과, 권한 없음, 존재하지 않는 사용자)")
        void testEventParticipationFailureCases() {
            // Case 1: 이미 참여 신청한 이벤트에 다시 신청
            JoinEventRequest joinRequest = new JoinEventRequest(regularUser1.getUserId());
            HttpEntity<JoinEventRequest> entity = new HttpEntity<>(joinRequest, regularUser1Headers);

            // 첫 번째 신청 (성공)
            restTemplate.postForEntity(
                    baseEventUrl + "/" + joinableEvent.getId() + "/participants", 
                    entity, 
                    String.class);

            // 두 번째 신청 (실패)
            ResponseEntity<String> duplicateResponse = restTemplate.postForEntity(
                    baseEventUrl + "/" + joinableEvent.getId() + "/participants", 
                    entity, 
                    String.class);

            assertThat(duplicateResponse.getStatusCode().is4xxClientError()).isTrue();

            // Case 2: 정원이 찬 이벤트에 참여 신청
            ResponseEntity<String> fullEventResponse = restTemplate.postForEntity(
                    baseEventUrl + "/" + fullEvent.getId() + "/participants", 
                    entity, 
                    String.class);

            assertThat(fullEventResponse.getStatusCode().is4xxClientError()).isTrue();

            // Case 3: 매니저가 아닌 사용자가 참여 요청 목록 조회
            HttpEntity<String> nonManagerEntity = new HttpEntity<>(null, regularUser1Headers);
            ResponseEntity<String> unauthorizedListResponse = restTemplate.exchange(
                    baseEventUrl + "/" + joinableEvent.getId() + "/participants/requested", 
                    HttpMethod.GET, 
                    nonManagerEntity, 
                    String.class);

            assertThat(unauthorizedListResponse.getStatusCode().is4xxClientError()).isTrue();

            // Case 4: 매니저가 아닌 사용자가 참여 승인 시도
            HttpEntity<Void> unauthorizedApproveEntity = new HttpEntity<>(null, regularUser2Headers);
            ResponseEntity<String> unauthorizedApproveResponse = restTemplate.exchange(
                    baseEventUrl + "/" + joinableEvent.getId() + "/participants/" + regularUser1.getUserId() + "/approve", 
                    HttpMethod.POST, 
                    unauthorizedApproveEntity, 
                    String.class);

            assertThat(unauthorizedApproveResponse.getStatusCode().is4xxClientError()).isTrue();

            // Case 5: 존재하지 않는 사용자 참여 승인 시도
            HttpEntity<Void> approveEntity = new HttpEntity<>(null, creatorHeaders);
            long nonExistentUserId = 99999L;
            ResponseEntity<String> nonExistentUserResponse = restTemplate.exchange(
                    baseEventUrl + "/" + joinableEvent.getId() + "/participants/" + nonExistentUserId + "/approve", 
                    HttpMethod.POST, 
                    approveEntity, 
                    String.class);

            assertThat(nonExistentUserResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
