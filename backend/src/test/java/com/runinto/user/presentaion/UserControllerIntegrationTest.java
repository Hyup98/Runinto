package com.runinto.user.presentaion;

import com.runinto.user.domain.Gender;
import com.runinto.user.domain.Role;
import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserJpaRepository;
import com.runinto.user.dto.request.UpdateProfileRequest;
import com.runinto.user.dto.response.ProfileResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @LocalServerPort
    private int port;

    private String baseUrl;
    private User testUser;

    @BeforeEach
    void setUp() {
        userJpaRepository.deleteAll();

        baseUrl = "http://localhost:" + port + "/users";

        testUser = User.builder()
                .name("통합유저")
                .email("integration@example.com")
                .password("1234")
                .imgUrl("img.jpg")
                .description("테스트용")
                .gender(Gender.MALE)
                .age(28)
                .role(Role.USER)
                .eventParticipants(new HashSet<>())
                .build();

        userJpaRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("프로필 조회 성공")
    void testGetProfile_success() {
        ResponseEntity<ProfileResponse> response =
                restTemplate.getForEntity(baseUrl + "/profile/" + testUser.getUserId(), ProfileResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("통합유저");
        assertThat(response.getBody().getEmail()).isEqualTo("integration@example.com");
    }

    @Test
    @DisplayName("프로필 조회 실패 - 존재하지 않는 유저")
    void testGetProfile_notFound() {
        ResponseEntity<String> response =
                restTemplate.getForEntity(baseUrl + "/profile/99999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void testUpdateProfile_success() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("업데이트유저")
                .age(33)
                .gender(Gender.FEMALE)
                .description("업데이트 설명")
                .imgUrl("updated.jpg")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UpdateProfileRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProfileResponse> response = restTemplate.exchange(
                baseUrl + "/profile/" + testUser.getUserId(),
                HttpMethod.PATCH,
                entity,
                ProfileResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("업데이트유저");
        assertThat(response.getBody().getImgUrl()).isEqualTo("updated.jpg");
        assertThat(response.getBody().getGender()).isEqualTo(Gender.FEMALE);
        assertThat(response.getBody().getAge()).isEqualTo(33);
        assertThat(response.getBody().getDescription()).isEqualTo("업데이트 설명");
    }

    @Test
    @DisplayName("프로필 수정 실패 - 유저 없음")
    void testUpdateProfile_notFound() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("없는유저")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UpdateProfileRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/profile/99999",
                HttpMethod.PATCH,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("참여한 이벤트 조회 - 초기엔 없음")
    void testGetJoinedEvents_empty() {
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
                baseUrl + "/" + testUser.getUserId() + "/joined-events",
                Object[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }
}
