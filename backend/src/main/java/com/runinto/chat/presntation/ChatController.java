package com.runinto.chat.presntation;

import com.runinto.chat.domain.repository.message.ChatMessage;
import com.runinto.chat.domain.repository.chatroom.Chatroom;
import com.runinto.chat.domain.repository.chatroom.ChatroomParticipant;
import com.runinto.chat.dto.request.ChatMessageRequest;
import com.runinto.chat.dto.response.ChatMessageResponse;
import com.runinto.chat.dto.response.ChatroomResponse;
import com.runinto.chat.dto.response.ParticipantResponse;
import com.runinto.chat.service.ChatService;
import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/events/{eventId}/chatroom")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserJpaRepository userRepository;

    // 채팅방 생성 요청
    @PostMapping
    public ResponseEntity<Void> createChatroomV1(@PathVariable Long eventId) {
        chatService.createChatRoomForEvent(eventId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 채팅방 조회 요청
    @GetMapping
    public ResponseEntity<ChatroomResponse> getChatroom(@PathVariable Long eventId) {
        Optional<Chatroom> chatroom = chatService.findChatroomByEventId(eventId);
        return chatroom.map(room -> ResponseEntity.ok(new ChatroomResponse(room.getId())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 채팅방 삭제 요청
    @DeleteMapping
    public ResponseEntity<Void> deleteChatroom(@PathVariable Long eventId) {
        Optional<Chatroom> chatroom = chatService.findChatroomByEventId(eventId);
        if (chatroom.isPresent()) {
            chatService.deleteChatroom(chatroom.get().getId());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 메시지 전송 요청
    @PostMapping("/messages")
    public ResponseEntity<Void> sendMessage(@PathVariable Long eventId, @RequestBody ChatMessageRequest request) {
        Optional<Chatroom> chatroom = chatService.findChatroomByEventId(eventId);
        if (chatroom.isPresent()) {
            ChatMessage message = ChatMessage.builder()
                    .chatroom(chatroom.get())
                    .senderId(request.getSenderId())
                    .message(request.getMessage())
                    .sendTime(new Time(System.currentTimeMillis()))
                    .build();
            chatService.sendMessage(message);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 메시지 목록 요청
    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long eventId) {
        Optional<Chatroom> chatroom = chatService.findChatroomByEventId(eventId);
        if (chatroom.isPresent()) {
            Optional<List<ChatMessage>> messages = chatService.findChatMessagesByRoom(chatroom.get());
            return ResponseEntity.ok(
                    messages.orElse(List.of()).stream()
                            .map(ChatMessageResponse::from)
                            .toList()
            );
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 참여자 목록 요청
    @GetMapping("/participants")
    public ResponseEntity<List<ParticipantResponse>> getParticipants(@PathVariable Long eventId) {
        Optional<Chatroom> chatroom = chatService.findChatroomByEventId(eventId);
        if (chatroom.isPresent()) {
            List<ParticipantResponse> responses = chatroom.get().getParticipants().stream()
                    .map(ChatroomParticipant::getUser)
                    .map(ParticipantResponse::from)
                    .toList();
            return ResponseEntity.ok(responses);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 채팅방 참여 요청
    @PostMapping("/participants")
    public ResponseEntity<Void> joinChatroom(@PathVariable Long eventId, @RequestParam Long userId) {
        Optional<Chatroom> chatroom = chatService.findChatroomByEventId(eventId);
        Optional<User> user = userRepository.findById(userId);
        if (chatroom.isPresent() && user.isPresent()) {
            chatService.addParticipant(chatroom.get(), user.get());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 채팅방 퇴장 요청
    @DeleteMapping("/participants")
    public ResponseEntity<Void> leaveChatroom(@PathVariable Long eventId, @RequestParam Long userId) {
        Optional<Chatroom> chatroom = chatService.findChatroomByEventId(eventId);
        if (chatroom.isPresent()) {
            chatService.removeParticipant(chatroom.get(), userId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}