package com.runinto.user.service;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventParticipant;
import com.runinto.user.domain.Gender;
import com.runinto.user.domain.Role;
import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserH2Repository;
import com.runinto.user.domain.repository.UserMemoryRepository;
import com.runinto.user.dto.response.EventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserH2Repository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("getUser")
    void getUser() {
        // given
        User dummyUser = User.builder()
                .age(1)
                .description("dumy")
                .email("dumy@gmail.com")
                .role(Role.USER)
                .gender(Gender.FEMALE)
                .imgUrl("URL")
                .name("더미유저")
                .password("password")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(dummyUser));

        // when
        Optional<User> result = userService.getUser(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("더미유저");

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getUser -> 아이디 없음")
    void GetNonUser() {
        // given
        User dummyUser = User.builder()
                .age(1)
                .description("dumy")
                .email("dumy@gmail.com")
                .role(Role.USER)
                .gender(Gender.FEMALE)
                .imgUrl("URL")
                .name("더미유저")
                .password("password")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        Optional<User> result = userService.getUser(1L);

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("saveUser -> 새로운 유저")
    void saveUser() {
        // given
        User dummyUser = User.builder()
                .age(1)
                .description("dumy")
                .email("dumy@gmail.com")
                .role(Role.USER)
                .gender(Gender.FEMALE)
                .imgUrl("URL")
                .name("더미유저")
                .password("password")
                .build();
        // when
        userService.saveUser(dummyUser);

        // then
        verify(userRepository, times(1)).save(dummyUser); // 🔹 save가 정확히 한 번 호출됐는지
    }

    @Test
    @DisplayName("saveUser -> 기존 유저")
    void saveUserExist() {
        // given
        User dummyUser = User.builder()
                .age(1)
                .description("dumy")
                .email("dumy@gmail.com")
                .role(Role.USER)
                .gender(Gender.FEMALE)
                .imgUrl("URL")
                .name("더미유저")
                .password("password")
                .build();
        // when
        userService.saveUser(dummyUser);

        // then
        verify(userRepository, times(1)).save(dummyUser); // 🔹 save가 정확히 한 번 호출됐는지
    }

    @Test
    @DisplayName("joined Event -> 참여한 이벤트 없음")
    void joinedEvent() {
        // given
        Long userId = 1L;
        when(userRepository.findJoinedEvents(userId)).thenReturn(Collections.emptyList());

        // when
        List<EventResponse> result = userService.getJoinedEvents(userId);

        // then
        assertTrue(result.isEmpty());
    }
}
