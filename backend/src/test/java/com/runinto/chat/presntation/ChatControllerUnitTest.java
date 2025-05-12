package com.runinto.chat.presntation;

import com.runinto.chat.domain.repository.chatroom.Chatroom;
import com.runinto.chat.domain.repository.message.ChatMessage;
import com.runinto.chat.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/*

@ExtendWith(MockitoExtension.class)
class ChatControllerUnitTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
    }

    @Test
    @DisplayName("채팅방 생성 테스트")
    void createChatroomV1() throws Exception {
        // given
        Chatroom chatroom = new Chatroom(1L, "Test Chatroom");
        when(chatService.createChatRoom(any())).thenReturn(chatroom);

        // when & then
        mockMvc.perform(post("/chatrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Chatroom\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Chatroom"));
    }

    @Test
    @DisplayName("채팅방 조회 테스트")
    void getChatroom() throws Exception {
        // given
        Chatroom chatroom = new Chatroom(1L, "Test Chatroom");
        when(chatService.findChatroomById(1L)).thenReturn(Optional.of(chatroom));

        // when & then
        mockMvc.perform(get("/chatrooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Chatroom"));
    }

    @Test
    @DisplayName("채팅방 삭제 테스트")
    void deleteChatroom() throws Exception {
        // given
        when(chatService.deleteChatroom(1L)).thenReturn(true);

        // when & then
        mockMvc.perform(delete("/chatrooms/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Chatroom deleted successfully"));
    }

    @Test
    @DisplayName("메시지 전송 테스트")
    void sendMessage() throws Exception {
        // given
        ChatMessage message = ChatMessage.builder()
                .message("Test message")
                .senderId(1L)
                .build();
        when(chatService.sendMessage(any())).thenReturn(message);

        // when & then
        mockMvc.perform(post("/chatrooms/1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Test message\",\"senderId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Test message"))
                .andExpect(jsonPath("$.senderId").value(1));
    }

    @Test
    @DisplayName("메시지 목록 조회 테스트")
    void getMessages() throws Exception {
        // given
        List<ChatMessage> messages = Arrays.asList(
            ChatMessage.builder().message("Message 1").senderId(1L).build(),
            ChatMessage.builder().message("Message 2").senderId(2L).build()
        );
        when(chatService.findChatMessagesByRoomId(1L)).thenReturn(messages);

        // when & then
        mockMvc.perform(get("/chatrooms/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Message 1"))
                .andExpect(jsonPath("$[1].message").value("Message 2"));
    }

    @Test
    @DisplayName("참가자 목록 조회 테스트")
    void getParticipants() throws Exception {
        // given
        List<Long> participants = Arrays.asList(1L, 2L, 3L);
        when(chatService.getParticipants(1L)).thenReturn(participants);

        // when & then
        mockMvc.perform(get("/chatrooms/1/participants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[1]").value(2))
                .andExpect(jsonPath("$[2]").value(3));
    }

    @Test
    @DisplayName("채팅방 참여 테스트")
    void joinChatroom() throws Exception {
        // given
        when(chatService.addParticipant(1L, 1L)).thenReturn(true);

        // when & then
        mockMvc.perform(post("/chatrooms/1/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully joined the chatroom"));
    }

    @Test
    @DisplayName("채팅방 나가기 테스트")
    void leaveChatroom() throws Exception {
        // given
        when(chatService.removeParticipant(1L, 1L)).thenReturn(true);

        // when & then
        mockMvc.perform(delete("/chatrooms/1/participants/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully left the chatroom"));
    }
}*/
