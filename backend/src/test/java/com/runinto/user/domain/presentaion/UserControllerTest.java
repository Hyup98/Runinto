package com.runinto.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runinto.user.domain.Gender;
import com.runinto.user.domain.User;
import com.runinto.user.dto.response.ProfileResponse;
import com.runinto.user.presentaion.UserController;
import com.runinto.user.service.UserService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .description("Test User")
                .gender(Gender.MALE)
                .age(25)
                .imgUrl("https://image.url/profile.jpg")
                .name("Test User")
                .userId(1L)
                .build();
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(userService);
    }

    @Test
    void getProfile() throws Exception {
        // given
        Mockito.when(userService.getUser(1L)).thenReturn(Optional.of(testUser));

        ProfileResponse expected = ProfileResponse.from(testUser);

        // when & then
        mockMvc.perform(get("/users/profile/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(expected.getName()))
                .andExpect(jsonPath("$.age").value(expected.getAge()))
                .andExpect(jsonPath("$.gender").value(expected.getGender().toString()))
                .andExpect(jsonPath("$.description").value(expected.getDescription()))
                .andExpect(jsonPath("$.imgUrl").value(expected.getImgUrl()));
    }

    @Test
    void getProfile_notFound() throws Exception {
        // given
        Mockito.when(userService.getUser(anyLong())).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/users/profile/999"))
                .andExpect(status().isNotFound());
    }
}