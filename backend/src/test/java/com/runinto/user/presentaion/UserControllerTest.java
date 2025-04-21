package com.runinto.user.presentaion;


import com.runinto.user.domain.Gender;
import com.runinto.user.domain.User;
import com.runinto.user.dto.request.UpdateProfileRequest;
import com.runinto.user.dto.response.ProfileResponse;

import com.runinto.user.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private UserService userService;

    @LocalServerPort
    private int port;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/users";
    }

    //region 유저 조회
    @Test
    @DisplayName("유저 조회")
    void getProfile() {

        //given
        User dummyUser = new User(1L, "김영희", "IMGURL", "여자", Gender.MALE, 20);

        //when
        when(userService.getUser(any())).thenReturn(Optional.of(dummyUser));

        String url = baseUrl + "/profile/1";

        ResponseEntity<ProfileResponse> response =
                restTemplate.getForEntity(url, ProfileResponse.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        String name = response.getBody().getName();

        assertThat(name).isEqualTo("김영희");
        assertThat(response.getBody().getGender()).isEqualTo(Gender.MALE);
        assertThat(response.getBody().getAge()).isEqualTo(20);
    }

    @Test
    @DisplayName("유저 조회-> id가 없을때")
    void getProfileNonId() {

        //given
        User dummyUser = new User(1L, "김영희", "IMGURL", "여자", Gender.MALE, 20);

        //when
        when(userService.getUser(any())).thenReturn(Optional.empty());

        String url = baseUrl + "/profile/4";

        ResponseEntity<ProfileResponse> response =
                restTemplate.getForEntity(url, ProfileResponse.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        System.out.println("응답 본문: " + response.getBody());
    }

    @Test
    @DisplayName("유저 조회 -> id가 유효하지 않을때(음수)")
    void getProfileInvalidIdV1() {

        //given
        User dummyUser = new User(1L, "김영희", "IMGURL", "여자", Gender.MALE, 20);

        //when
        when(userService.getUser(any())).thenReturn(Optional.empty());

        String url = baseUrl + "/profile/-1";

        ResponseEntity<ProfileResponse> response =
                restTemplate.getForEntity(url, ProfileResponse.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        System.out.println("응답 본문: " + response.getBody());
    }

    @Test
    @DisplayName("유저 조회 -> id가 유효하지 않을때(문자)")
    void getProfileInvalidIdV2() {

        //given
        User dummyUser = new User(1L, "김영희", "IMGURL", "여자", Gender.MALE, 20);

        //when
        when(userService.getUser(any())).thenReturn(Optional.empty());

        String url = baseUrl + "/profile/asfmk1@!1";

        ResponseEntity<ProfileResponse> response =
                restTemplate.getForEntity(url, ProfileResponse.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        System.out.println("응답 본문: " + response.getBody());
    }
    //endregion

    //region 프로필 업데이터
    @Test
    @DisplayName("유저 프로필 업데이트")
    void updateProfile() {

        //given
        User dummyUser = new User(1L, "김영희", "IMGURL", "여자", Gender.MALE, 20);
        when(userService.getUser(any())).thenReturn(Optional.of(dummyUser));
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .age(13)
                .profileImg("newIMGURL")
                .description("newDescription")
                .gender(Gender.MALE)
                .name("newName")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateProfileRequest> entity = new HttpEntity<>(request, headers);
        String url = baseUrl + "/profile/1";

        //when
        ResponseEntity<ProfileResponse> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                entity,
                ProfileResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("newName");

        System.out.println("응답 본문: " + response.getBody());
    }

    @Test
    @DisplayName("유저 프로필 업데이트 -> 나이가 음수면 처리 x + 안보내면 기존 내용 유지")
    void updateProfileNonId() {

        //given
        User dummyUser = new User(1L, "김영희", "IMGURL", "여자", Gender.MALE, 20);
        when(userService.getUser(any())).thenReturn(Optional.of(dummyUser));
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .age(- 13)
                .profileImg("newIMGURL")
                .description("newDescription")
                .gender(Gender.MALE)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateProfileRequest> entity = new HttpEntity<>(request, headers);
        String url = baseUrl + "/profile/1";

        //when
        ResponseEntity<ProfileResponse> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                entity,
                ProfileResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("김영희");
        assertThat(response.getBody().getAge()).isEqualTo(20);

        System.out.println("응답 본문: " + response.getBody());
    }
    //endregion

}