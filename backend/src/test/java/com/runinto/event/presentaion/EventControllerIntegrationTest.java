package com.runinto.event.presentaion;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

//https://meetup.nhncloud.com/posts/223
/*
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

                EventCategory category = new EventCategory((long) i, type, (long) i);
                Event event = Event.builder()
                        .eventId((long) i)
                        .title("이벤트 " + i)
                        .description("설명 " + i)
                        .latitude(lat)
                        .longitude(lng)
                        .categories(Set.of(category))
                        .maxParticipants(5)
                        .build();

                eventService.save(event);
            }
        }
    }

    @AfterEach
    void tearDown() {
        eventService.clear(); // clear 메소드 구현 필요
    }

    //region 이벤트 목록 조회
    @Test
    @DisplayName("카테고리 및 위치 필터로 이벤트 목록 조회")
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

        List<Event> result = response.getBody().getEvents();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(filteredEvents.size());

        // 예상
        List<String> expectedTitles = filteredEvents.stream()
                .map(Event::getTitle)
                .collect(Collectors.toList());

        // 실제
        List<String> actualTitles = result.stream()
                .map(Event::getTitle)
                .collect(Collectors.toList());
    }

    @Test
    @DisplayName("이벤트 목록 조회 필수 파라미터가 더 많다-> swLat 추가")
    void testMissingRequiredParamsV1() {
        // given & when

        String url = baseUrl +
                "?swLat=37.50&neLat=37.60&?Lat=37.510" +
                "&swLng=127.00&neLng=127.10" +
                "&category=ACTIVITY" +
                "&isPublic=true";

        ResponseEntity<EventListResponse> response =
                restTemplate.getForEntity(url, EventListResponse.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println("응답 본문: " + response.getBody());
    }

    @Test
    @DisplayName("이벤트 목록 조회 필수 파라미터 누락-> swLat, neLng누락")
    void testMissingRequiredParamsV2() {
        //given & when
        String url = baseUrl + "?neLat=37.60&swLng=127.00";  // swLat, neLng 빠짐

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        System.out.println("응답 본문: " + response.getBody());

    }

    @Test
    @DisplayName("이벤트 목록 조회 파라미터 잘못된 타입-> EventType")
    void testMissingRequiredParamsV3() {
        // given & when

        String url = baseUrl +
                "?swLat=37.50&neLat=37.60" +
                "&swLng=127.00&neLng=127.10" +
                "&category=GAME" +
                "&isPublic=true";
        ResponseEntity<EventListResponse> response =
                restTemplate.getForEntity(url, EventListResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        System.out.println("응답 본문: " + response.getBody());
    }

    @Test
    @DisplayName("이벤트 목록 조회 파라미터 잘못된 타입-> 위도 경도가 숫자x")
    void testMissingRequiredParamsV4() {
        // given & when

        String url = baseUrl +
                "?swLat=asfa40&neLat=37.60" +
                "&swLng=127.00&neLng=127.10" +
                "&category=ACTIVITY" +
                "&isPublic=true";

        ResponseEntity<EventListResponse> response =
                restTemplate.getForEntity(url, EventListResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        System.out.println("응답 본문: " + response.getBody());

    }

    //todo -> 에러 메시지 코드를 추가할까? ... 흠
    @Test
    @DisplayName("이벤트 목록 조회 파라미터 범위 에러-> EventType")
    void testMissingRequiredParamsV5() {
        // given & when
        String url = baseUrl +
                "?swLat=1837.50&neLat=37.60" +
                "&swLng=127.00&neLng=1027.10" +
                "&category=ACTIVITY" +
                "&isPublic=true";

        ResponseEntity<ErrorResponse> response =
                restTemplate.getForEntity(url, ErrorResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        System.out.println("응답 본문: " + response.getBody());
    }
    //endregion

    //region 이벤트 업데이트
    @Test
    @DisplayName("이벤트 업데이트 테스트.")
    void testUpdateEvent() {
        UpdateEventRequest request = new UpdateEventRequest("새 타이틀", "새 설명", 20, 37.8, 127.5, true);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateEventRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<EventResponse> response = restTemplate.exchange(baseUrl + "/1", HttpMethod.PATCH, entity, EventResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTitle()).isEqualTo("새 타이틀");
    }
    //endregion

    //region 이벤트 참여
    @Test
    @DisplayName("이벤트 참여 요청 테스트.")
    void testAddParticipant() {
        JoinEventRequest request = new JoinEventRequest(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JoinEventRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/1/participants", entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("성공적으로 참여");
    }

    @Test
    @DisplayName("이벤트 참여 요청 테스트. -> 존재하지 않는 이벤트 참여")
    void testAddParticipantinNon() {
        JoinEventRequest request = new JoinEventRequest(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JoinEventRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/11/participants", entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        System.out.println("응답 본문: " + response.getBody());
    }

    //endregion

    //region 이벤트 삭제
    @Test
    @DisplayName("이벤트 삭제 테스트.")
    void testDeleteEvent() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/1", HttpMethod.DELETE, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("deleted");
        assertThat(eventService.findAll().size()).isEqualTo(9);
        for(Event event : eventService.findAll()) {
            log.info(event.toString());
        }
    }

    @Test
    @DisplayName("이벤트 삭제 테스트0. -> 존재 하지 않는 이벤트 삭제 요청")
    void testDeleteNonEvent() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/0", HttpMethod.DELETE, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(eventService.findAll().size()).isEqualTo(10);
        for(Event event : eventService.findAll()) {
            log.info(event.toString());
        }
    }
    //endregion
}*/
