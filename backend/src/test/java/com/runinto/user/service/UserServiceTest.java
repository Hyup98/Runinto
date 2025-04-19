package com.runinto.user.service;

import com.runinto.user.domain.repository.UserMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

class UserServiceTest {

    private UserMemoryRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserMemoryRepository.class); // 🔹 목 객체 생성
        userService = new UserService(userRepository);     // 🔹 주입
    }

    @Test
    void getUser() {
    }

    @Test
    void saveUser() {
    }
}