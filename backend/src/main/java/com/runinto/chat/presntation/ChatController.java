package com.runinto.chat.presntation;

import com.runinto.chat.domain.ChatMessage;
import com.runinto.chat.dto.request.ChatMessageResponse;
import com.runinto.chat.dto.response.ChatMessageRequest;
import com.runinto.chat.dto.response.ChatroomResponse;
import com.runinto.chat.dto.response.ParticipantResponse;
import com.runinto.chat.service.ChatService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.util.List;

@RestController
@RequestMapping("/events/{eventId}/chatroom")
public class ChatController {

    private final ChatService chatService;

    public ChatController(final ChatService chatService) {
        this.chatService = chatService;
    }

    // 채팅방 생성 요청
    @PostMapping
    public ResponseEntity<Void> createChatroom(@PathVariable Long eventId) {
        // TODO: 채팅방 생성 로직
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 채팅방 조회 요청
    @GetMapping
    public ResponseEntity<ChatroomResponse> getChatroom(@PathVariable Long eventId) {
        // TODO: 채팅방 조회 로직
        return ResponseEntity.ok().build();
    }

    // 채팅방 삭제 요청
    @DeleteMapping
    public ResponseEntity<Void> deleteChatroom(@PathVariable Long eventId) {
        // TODO: 채팅방 삭제 로직
        return ResponseEntity.noContent().build();
    }

    // 메시지 전송 요청
    @PostMapping("/messages")
    public ResponseEntity<Void> sendMessage(
            @PathVariable Long eventId,
            @RequestBody ChatMessageRequest request
    ) {
        // TODO: 메시지 전송 로직
        return ResponseEntity.ok().build();
    }

    // 메시지 목록 요청
    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long eventId) {
        // TODO: 메시지 목록 조회 로직
        return ResponseEntity.ok().build();
    }

    // 참여자 목록 요청
    @GetMapping("/participants")
    public ResponseEntity<List<ParticipantResponse>> getParticipants(@PathVariable Long eventId) {
        // TODO: 참여자 목록 조회 로직
        return ResponseEntity.ok().build();
    }

    // 채팅방 참여 요청
    @PostMapping("/participants")
    public ResponseEntity<Void> joinChatroom(@PathVariable Long eventId) {
        // TODO: 참여 로직
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 채팅방 퇴장 요청
    @DeleteMapping("/participants")
    public ResponseEntity<Void> leaveChatroom(@PathVariable Long eventId) {
        // TODO: 퇴장 로직
        return ResponseEntity.noContent().build();
    }
}
