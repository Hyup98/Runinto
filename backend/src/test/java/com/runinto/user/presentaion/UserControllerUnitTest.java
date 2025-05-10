package com.runinto.user.presentaion;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.runinto.user.domain.Gender;
import com.runinto.user.domain.User;
import com.runinto.user.dto.request.UpdateProfileRequest;
import com.runinto.user.dto.response.EventResponse;
import com.runinto.user.dto.response.ProfileResponse;
import com.runinto.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.patch;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/*

@WebMvcTest(UserController.class)
class UserControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("사용자 프로필 조회 성공")
    void getProfile_success() throws Exception {
        Long userId = 1L;
        User dummyUser = createDummyUser(userId);
        ProfileResponse profile = ProfileResponse.from(dummyUser);

        when(userService.getUser(userId)).thenReturn(Optional.of(dummyUser));

        mockMvc.perform(get("/users/profile/{user_id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트유저"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("사용자 프로필 조회 실패 - 존재하지 않음")
    void getProfile_notFound() throws Exception {
        when(userService.getUser(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/profile/{user_id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("사용자 프로필 수정 성공")
    void updateProfile_success() throws Exception {
        Long userId = 1L;
        User dummyUser = createDummyUser(userId);
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("수정유저")
                .age(25)
                .gender(Gender.FEMALE)
                .description("수정된 설명")
                .imgUrl("newimg.jpg")
                .build();

        when(userService.getUser(userId)).thenReturn(Optional.of(dummyUser));
        doNothing().when(userService).saveUser(any(User.class));

        mockMvc.perform(patch("/users/profile/{user_id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정유저"))
                .andExpect(jsonPath("$.imgUrl").value("newimg.jpg"));
    }

    @Test
    @DisplayName("사용자 프로필 수정 실패 - 유저 없음")
    void updateProfile_notFound() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("없는유저")
                .build();

        when(userService.getUser(999L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/users/profile/{user_id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("참여한 이벤트 없음")
    void getJoinedEvents_empty() throws Exception {
        when(userService.getJoinedEvents(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/{userId}/joined-events", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("참여한 이벤트 존재")
    void getJoinedEvents_success() throws Exception {
        List<EventResponse> events = List.of(
                new EventResponse(1L, "이벤트1", "설명", 10, 3, 37.5, 127.0, List.of())
        );
        when(userService.getJoinedEvents(1L)).thenReturn(events);

        mockMvc.perform(get("/users/{userId}/joined-events", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("이벤트1"));
    }

    private User createDummyUser(Long userId) {
        return User.builder()
                .userId(userId)
                .name("테스트유저")
                .email("test@example.com")
                .password("1234")
                .imgUrl("img.jpg")
                .description("설명")
                .gender(Gender.MALE)
                .age(30)
                .role(Role.USER)
                .eventParticipants(Collections.emptySet())
                .build();
    }
}
*/
