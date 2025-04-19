package com.runinto.user.service;

import com.runinto.user.domain.Gender;
import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMemoryRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("getUser")
    void getUser() {
        // given
        User dummyUser = new User(3L, "김영희", "IMGURL", "여자", Gender.MALE, 20);
        when(userRepository.findById(1L)).thenReturn(Optional.of(dummyUser));

        // when
        Optional<User> result = userService.getUser(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("김영희");

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getUser -> 아이디 없음")
    void GetNonUser() {
        // given
        User dummyUser = new User(3L, "김영희", "IMGURL", "여자", Gender.MALE, 20);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        Optional<User> result = userService.getUser(1L);

        // then
        assertThat(result).isNotPresent();

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("saveUser -> 새로운 유저")
    void saveUser() {
        // given
        User user = new User(2L, "김영희", "IMGURL", "여자", Gender.MALE, 20);

        // when
        userService.saveUser(user);

        // then
        verify(userRepository, times(1)).save(user); // 🔹 save가 정확히 한 번 호출됐는지
    }

    @Test
    @DisplayName("saveUser -> 기존 유저")
    void saveUseExist() {
        // given
        User user = new User(1L, "김영희", "IMGURL", "여자", Gender.MALE, 20);

        // when
        userService.saveUser(user);

        // then
        verify(userRepository, times(1)).save(user); // 🔹 save가 정확히 한 번 호출됐는지
    }
}