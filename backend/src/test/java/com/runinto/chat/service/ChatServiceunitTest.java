package com.runinto.chat.service;

import com.runinto.chat.domain.repository.chatroom.Chatroom;
import com.runinto.chat.domain.repository.chatroom.ChatroomH2Repository;
import com.runinto.chat.domain.repository.message.ChatMessage;
import com.runinto.chat.domain.repository.message.ChatMessageH2Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
/*

@ExtendWith(MockitoExtension.class)
class ChatServiceunitTest {

    @Mock
    private ChatroomH2Repository chatroomRepository;

    @Mock
    private ChatMessageH2Repository chatMessageRepository;

    @InjectMocks
    private ChatService chatService;

    private Chatroom testChatroom;
    private ChatMessage testMessage;

    @BeforeEach
    void setUp() {
        testChatroom = new Chatroom(1L, "Test Chatroom");
        testMessage = ChatMessage.builder()
                .message("Test message")
                .senderId(1L)
                .build();
    }

    @Test
    @DisplayName("채팅방 ID로 조회")
    void findChatroomById() {
        // given
        when(chatroomRepository.findById(1L)).thenReturn(Optional.of(testChatroom));

        // when
        Optional<Chatroom> result = chatService.findChatroomById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Test Chatroom");
    }

    @Test
    @DisplayName("채팅방의 메시지 목록 조회")
    void findChatMessagesByRoomId() {
        // given
        List<ChatMessage> messages = Arrays.asList(
            ChatMessage.builder().message("Message 1").senderId(1L).build(),
            ChatMessage.builder().message("Message 2").senderId(2L).build()
        );
        when(chatMessageRepository.findByChatroomId(1L)).thenReturn(messages);

        // when
        List<ChatMessage> result = chatService.findChatMessagesByRoomId(1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMessage()).isEqualTo("Message 1");
        assertThat(result.get(1).getMessage()).isEqualTo("Message 2");
    }

    @Test
    @DisplayName("새 채팅방 생성")
    void createChatRoom() {
        // given
        when(chatroomRepository.save(any(Chatroom.class))).thenReturn(testChatroom);

        // when
        Chatroom result = chatService.createChatRoom(testChatroom);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Chatroom");
        verify(chatroomRepository).save(any(Chatroom.class));
    }

    @Test
    @DisplayName("메시지 전송")
    void sendMessage() {
        // given
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);

        // when
        ChatMessage result = chatService.sendMessage(testMessage);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Test message");
        assertThat(result.getSenderId()).isEqualTo(1L);
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("채팅방 삭제")
    void deleteChatroom() {
        // given
        when(chatroomRepository.existsById(1L)).thenReturn(true);
        doNothing().when(chatroomRepository).deleteById(1L);

        // when
        boolean result = chatService.deleteChatroom(1L);

        // then
        assertThat(result).isTrue();
        verify(chatroomRepository).deleteById(1L);
    }

    @Test
    @DisplayName("참가자 추가")
    void addParticipant() {
        // given
        when(chatroomRepository.findById(1L)).thenReturn(Optional.of(testChatroom));
        when(chatroomRepository.save(any(Chatroom.class))).thenReturn(testChatroom);

        // when
        boolean result = chatService.addParticipant(1L, 1L);

        // then
        assertThat(result).isTrue();
        verify(chatroomRepository).save(any(Chatroom.class));
    }

    @Test
    @DisplayName("참가자 제거")
    void removeParticipant() {
        // given
        when(chatroomRepository.findById(1L)).thenReturn(Optional.of(testChatroom));
        when(chatroomRepository.save(any(Chatroom.class))).thenReturn(testChatroom);

        // when
        boolean result = chatService.removeParticipant(1L, 1L);

        // then
        assertThat(result).isTrue();
        verify(chatroomRepository).save(any(Chatroom.class));
    }
}*/
