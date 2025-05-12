package com.runinto.chat.presntation;

import com.runinto.chat.domain.repository.message.ChatMessage;
import com.runinto.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

@Slf4j
@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatControllerIntegrationTest {


    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ChatService chatService;

    @LocalServerPort
    private int port;

    private String baseUrl;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("chatroom 생성")
    void createChatroomV1() {
    }

    @Test
    @DisplayName("chatroom 가져오기")
    void getChatroom() {
    }

    @Test
    @DisplayName("chatroom 삭제")
    void deleteChatroom() {
    }

    @Test
    @DisplayName("메시지 보내기")
    void sendMessage() {
    }

    @Test
    @DisplayName("메시지 가져오기")
    void getMessages() {
    }

    @Test
    @DisplayName("채팅방 참여자 가져오기")
    void getParticipants() {
    }

    @Test
    @DisplayName("chatroom 참여하기")
    void joinChatroom() {
    }

    @Test
    @DisplayName("chatroom 떠나기")
    void leaveChatroom() {
    }
}