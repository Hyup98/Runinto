package com.runinto.user.presentaion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runinto.auth.domain.SessionConst; // SessionConst 임포트 (프로젝트 경로에 맞게 수정)
import com.runinto.auth.domain.UserSessionDto; // UserSessionDto 임포트 (프로젝트 경로에 맞게 수정)
import com.runinto.user.domain.Gender;
import com.runinto.user.domain.Role;
import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserJpaRepository;
import com.runinto.user.dto.request.LoginRequest; // LoginRequest DTO 임포트 (프로젝트 경로 및 내용에 맞게 수정)
import com.runinto.user.dto.request.UpdateProfileRequest;
import com.runinto.user.dto.response.ProfileResponse;
// import com.runinto.user.dto.response.EventResponse; // EventResponse 사용 시 주석 해제
// import org.springframework.core.ParameterizedTypeReference; // List<EventResponse> 받을 때 필요
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private String baseUserUrl;
    private String baseAuthUrl;
    private User testUser;
    private String testUserPassword = "password123";

    @Value("${user.default-profile}")
    private String defaultProfilePath;

    private HttpHeaders authenticatedHeaders;

    @BeforeEach
    void setUp() {
        userJpaRepository.deleteAll();

        baseUserUrl = "http://localhost:" + port + "/users";
        baseAuthUrl = "http://localhost:" + port + "/auth"; // 실제 인증 API 경로

        testUser = User.builder()
                .name("통합유저")
                .email("integration@example.com")
                .password(testUserPassword) // 실제로는 해싱된 비밀번호
                .imgUrl(defaultProfilePath) // 초기 이미지는 기본 프로필 경로 사용 가능
                .description("테스트용")
                .gender(Gender.MALE)
                .age(28)
                .role(Role.USER)
                .eventParticipants(new HashSet<>())
                .build();
        userJpaRepository.save(testUser);

        // 로그인하여 세션 쿠키 획득
        authenticatedHeaders = authenticateAndGetHeaders(testUser.getEmail(), testUserPassword);
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
            // 실제 UserSessionDto가 세션에 저장되었는지 확인하는 로직 (예: /auth/me)이 있다면 더 좋습니다.
            // 이 테스트에서는 UserSessionDto의 구체적인 내용보다는 세션 존재 유무가 중요합니다.
            // SessionFilter가 UserSessionDto 타입을 검사하므로, 로그인 시 해당 타입으로 저장되어야 합니다.
            // 예시: session.setAttribute(SessionConst.LOGIN_MEMBER, new UserSessionDto(user.getUserId(), user.getRole()));
            return headers;
        }
        System.err.println("WARN: Authentication failed in setUp for user " + email + ". Subsequent tests might fail due to 401.");
        // 테스트 실패를 유도하기 위해 예외를 던지거나 Assertions.fail()을 사용할 수 있습니다.
        // throw new IllegalStateException("Authentication failed in test setup for user: " + email);
        return new HttpHeaders(); // 로그인 실패 시 테스트가 401을 받도록 빈 헤더 반환 (또는 예외 처리)
    }

    @AfterEach
    void tearDown() {
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("프로필 조회 성공 - 인증된 사용자")
    void testGetProfile_success_authenticated() {
        // setUp에서 이미 testUser가 생성되고 authenticatedHeaders가 준비됨
        HttpEntity<String> entity = new HttpEntity<>(null, authenticatedHeaders);

        ResponseEntity<ProfileResponse> response =
                restTemplate.exchange(baseUserUrl + "/profile/" + testUser.getUserId(), HttpMethod.GET, entity, ProfileResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProfileResponse responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getName()).isEqualTo(testUser.getName());
        assertThat(responseBody.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(responseBody.getImgUrl()).isEqualTo(testUser.getImgUrl());
    }

    @Test
    @DisplayName("프로필 조회 실패 - 인증되지 않은 사용자 (401 Unauthorized)")
    void testGetProfile_failure_unauthenticated() {
        ResponseEntity<String> response =
                restTemplate.getForEntity(baseUserUrl + "/profile/" + testUser.getUserId(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("프로필 조회 실패 - 존재하지 않는 유저 (인증된 사용자)")
    void testGetProfile_notFound_authenticated() {
        HttpEntity<String> entity = new HttpEntity<>(null, authenticatedHeaders);
        ResponseEntity<String> response =
                restTemplate.exchange(baseUserUrl + "/profile/99999", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("프로필 수정 성공 - 인증된 사용자, JSON 데이터만 (이미지 변경 없음)")
    void testUpdateProfile_success_authenticated_jsonOnly() throws Exception {
        UpdateProfileRequest requestDto = UpdateProfileRequest.builder()
                .name("수정된통합유저")
                .age(35)
                .gender(Gender.FEMALE)
                .description("설명이 수정되었습니다.")
                .build();

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        // JSON 데이터를 위한 파트 ("profile"은 @RequestPart 이름과 일치해야 함)
        // 중요: UserController의 UpdateProfile 메소드가 UpdateProfileRequest를 @RequestPart("profile")로 받는다고 가정합니다.
        // 만약 @RequestBody로 받고 있다면, 이 테스트는 실패하거나 수정되어야 합니다.
        HttpHeaders profileDtoHeaders = new HttpHeaders();
        profileDtoHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> profileDtoEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestDto), profileDtoHeaders);
        parts.add("profile", profileDtoEntity);
        // 이미지 파일 파트는 추가하지 않음

        HttpHeaders requestHeaders = new HttpHeaders(authenticatedHeaders); // 인증 쿠키 복사
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, requestHeaders);

        ResponseEntity<ProfileResponse> response = restTemplate.exchange(
                baseUserUrl + "/profile/" + testUser.getUserId(),
                HttpMethod.PATCH,
                requestEntity,
                ProfileResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProfileResponse responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getName()).isEqualTo("수정된통합유저");
        assertThat(responseBody.getAge()).isEqualTo(35);
        assertThat(responseBody.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(responseBody.getDescription()).isEqualTo("설명이 수정되었습니다.");
        assertThat(responseBody.getImgUrl()).isEqualTo(testUser.getImgUrl()); // 이미지는 변경되지 않음

        User updatedUserInDb = userJpaRepository.findById(testUser.getUserId()).orElseThrow();
        assertThat(updatedUserInDb.getName()).isEqualTo("수정된통합유저");
        assertThat(updatedUserInDb.getAge()).isEqualTo(35);
        assertThat(updatedUserInDb.getImgUrl()).isEqualTo(testUser.getImgUrl());
    }

    @Test
    @DisplayName("프로필 수정 성공 - 인증된 사용자, 이미지 포함")
    void testUpdateProfile_success_authenticated_withImage() throws Exception {
        UpdateProfileRequest requestDto = UpdateProfileRequest.builder()
                .name("이미지포함수정")
                .description("프로필 이미지도 변경합니다.")
                .build();

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

        HttpHeaders profileDtoHeaders = new HttpHeaders();
        profileDtoHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> profileDtoEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestDto), profileDtoHeaders);
        parts.add("profile", profileDtoEntity); // "profile" @RequestPart

        ByteArrayResource imageResource = new ByteArrayResource("new-image-data-bytes".getBytes()) {
            @Override
            public String getFilename() { // TestRestTemplate이 파일 이름을 인식하도록 설정
                return "updated-profile.jpg";
            }
        };
        // 파일 파트 헤더는 TestRestTemplate이 자동으로 설정하는 경우가 많으나, 명시적으로 추가할 수도 있습니다.
        // 여기서는 ByteArrayResource와 파일 이름만으로 충분할 수 있습니다.
        parts.add("image", imageResource); // "image" @RequestPart

        HttpHeaders requestHeaders = new HttpHeaders(authenticatedHeaders);
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, requestHeaders);

        ResponseEntity<ProfileResponse> response = restTemplate.exchange(
                baseUserUrl + "/profile/" + testUser.getUserId(),
                HttpMethod.PATCH,
                requestEntity,
                ProfileResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProfileResponse responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getName()).isEqualTo("이미지포함수정");
        assertThat(responseBody.getDescription()).isEqualTo("프로필 이미지도 변경합니다.");
        assertThat(responseBody.getImgUrl()).isNotNull().isNotEqualTo(testUser.getImgUrl()); // 이미지 URL 변경 확인

        User updatedUserInDb = userJpaRepository.findById(testUser.getUserId()).orElseThrow();
        assertThat(updatedUserInDb.getImgUrl()).isNotNull().isNotEqualTo(testUser.getImgUrl());
        // ImageStorageService의 saveImage 결과에 따라 실제 저장된 URL이 달라지므로, null이 아니고 기존과 다른지만 확인
    }

    @Test
    @DisplayName("프로필 수정 실패 - 유저 없음 (인증된 사용자)")
    void testUpdateProfile_notFound_authenticated() throws Exception {
        UpdateProfileRequest requestDto = UpdateProfileRequest.builder().name("없는유저").build();

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        HttpHeaders profileDtoHeaders = new HttpHeaders();
        profileDtoHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> profileDtoEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestDto), profileDtoHeaders);
        parts.add("profile", profileDtoEntity);

        HttpHeaders requestHeaders = new HttpHeaders(authenticatedHeaders);
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, requestHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUserUrl + "/profile/99999", // 존재하지 않는 사용자 ID
                HttpMethod.PATCH,
                requestEntity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("참여한 이벤트 조회 - 초기엔 없음 (인증된 사용자)")
    void testGetJoinedEvents_empty_authenticated() {
        HttpEntity<String> entity = new HttpEntity<>(null, authenticatedHeaders);

        ResponseEntity<Object[]> response = restTemplate.exchange( // 반환 타입을 Object[] 또는 구체적인 List<EventResponse>로 변경 가능
                baseUserUrl + "/" + testUser.getUserId() + "/joined-events",
                HttpMethod.GET,
                entity,
                Object[].class // 실제 EventResponse[] 타입으로 받을 수 있다면 더 좋습니다.
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }

    // 참여한 이벤트가 있는 경우의 테스트 (Event, EventParticipant, EventRepository 등이 필요)
    /*
    @Autowired // 실제 프로젝트에서는 EventRepository 주입
    private com.runinto.event.domain.repository.EventRepository eventRepository; // 실제 EventRepository 경로로 수정
    // @Autowired
    // private com.runinto.event.domain.repository.EventParticipantRepository eventParticipantRepository; // 실제 경로로 수정

    @Test
    @DisplayName("참여한 이벤트 조회 - 데이터 존재 (인증된 사용자)")
    void testGetJoinedEvents_withData_authenticated() {
        // 1. 테스트용 이벤트 생성 및 저장 (Event 엔티티와 Repository 필요)
        // com.runinto.event.domain.Event testEvent = com.runinto.event.domain.Event.builder().title("통합 테스트 이벤트").build();
        // eventRepository.save(testEvent);

        // 2. 테스트 사용자를 이벤트에 참여시킴 (EventParticipant 엔티티와 Repository 사용 등)
        // com.runinto.event.domain.EventParticipant participation = com.runinto.event.domain.EventParticipant.builder().user(testUser).event(testEvent).build();
        // eventParticipantRepository.save(participation);

        HttpEntity<String> entity = new HttpEntity<>(null, authenticatedHeaders);
        ResponseEntity<List<com.runinto.user.dto.response.EventResponse>> response = restTemplate.exchange(
            baseUserUrl + "/" + testUser.getUserId() + "/joined-events",
            HttpMethod.GET,
            entity,
            new org.springframework.core.ParameterizedTypeReference<List<com.runinto.user.dto.response.EventResponse>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getTitle()).isEqualTo("통합 테스트 이벤트");

        // 테스트 후 관련 데이터 정리
        // eventParticipantRepository.deleteAll();
        // eventRepository.deleteAll();
    }
    */
}