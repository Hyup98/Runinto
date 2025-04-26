package com.runinto.chat.presntation;

import com.runinto.chat.domain.ChatMessage;
import com.runinto.chat.domain.Chatroom;
import com.runinto.chat.dto.request.ChatMessageRequest;
import com.runinto.chat.dto.response.ChatMessageResponse;
import com.runinto.chat.dto.response.ChatroomResponse;
import com.runinto.chat.dto.response.ParticipantResponse;
import com.runinto.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/events/{eventId}/chatroom")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // 채팅방 생성 요청
    @PostMapping
    public ResponseEntity<Void> createChatroomV1(@PathVariable Long eventId) {
        Long chatRoomId = chatService.createChatRoom();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 채팅방 조회 요청
    @GetMapping
    public ResponseEntity<ChatroomResponse> getChatroom(@PathVariable Long eventId) {
        Optional<Chatroom> chatroom = chatService.findChatroomById(eventId);
        if (chatroom.isPresent()) {
            // TODO: Convert Chatroom to ChatroomResponse
            ChatroomResponse response = new ChatroomResponse(chatroom.get().getId());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 채팅방 삭제 요청
    @DeleteMapping
    public ResponseEntity<Void> deleteChatroom(@PathVariable Long eventId) {
        Optional<Chatroom> chatroom = chatService.findChatroomById(eventId);
        if (chatroom.isPresent()) {
            chatService.deleteChatroom(eventId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 메시지 전송 요청
    @PostMapping("/messages")
    public ResponseEntity<Void> sendMessage(
            @PathVariable Long eventId,
            @RequestBody ChatMessageRequest request
    ) {
        Optional<Chatroom> chatroom = chatService.findChatroomById(eventId);
        if (chatroom.isPresent()) {
            ChatMessage message = ChatMessage.builder()
                    .chatRoomId(eventId)
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
        Optional<Chatroom> chatroom = chatService.findChatroomById(eventId);
        if (chatroom.isPresent()) {
            Optional<List<ChatMessage>> messages = chatService.findChatMessagesByRoomId(eventId);
            if (messages.isPresent()) {
                List<ChatMessageResponse> responseList = messages.get().stream()
                        .map(ChatMessageResponse::from)
                        .toList();
                return ResponseEntity.ok(responseList);
            } else {
                return ResponseEntity.ok(List.of()); // Return empty list
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 참여자 목록 요청
    @GetMapping("/participants")
    public ResponseEntity<List<ParticipantResponse>> getParticipants(@PathVariable Long eventId) {
        Optional<Chatroom> chatroom = chatService.findChatroomById(eventId);
        if (chatroom.isPresent() && chatroom.get().getApplicants() != null) {
            // TODO: Convert applicants list to ParticipantResponse list
            return ResponseEntity.ok().build();
        } else if (chatroom.isPresent()) {
            return ResponseEntity.ok().build(); // Return empty list
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 채팅방 참여 요청
    @PostMapping("/participants")
    public ResponseEntity<Void> joinChatroom(
            @PathVariable Long eventId,
            @RequestParam Long userId
    ) {
        Optional<Chatroom> chatroom = chatService.findChatroomById(eventId);
        if (chatroom.isPresent()) {
            chatService.addParticipant(eventId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 채팅방 퇴장 요청
    @DeleteMapping("/participants")
    public ResponseEntity<Void> leaveChatroom(
            @PathVariable Long eventId,
            @RequestParam Long userId
    ) {
        Optional<Chatroom> chatroom = chatService.findChatroomById(eventId);
        if (chatroom.isPresent()) {
            chatService.removeParticipant(eventId, userId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
