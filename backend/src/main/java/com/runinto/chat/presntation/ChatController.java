package com.runinto.chat.presntation;

import com.runinto.chat.domain.repository.message.ChatMessage;
import com.runinto.chat.domain.repository.chatroom.Chatroom;
import com.runinto.chat.dto.request.ChatMessageRequest;
import com.runinto.chat.dto.response.ChatMessageResponse;
import com.runinto.chat.dto.response.ChatroomResponse;
import com.runinto.chat.dto.response.ParticipantResponse;
import com.runinto.chat.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/events/{eventId}/chatroom")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /*@PostMapping
    public ResponseEntity<ChatroomResponse> createChatroom(@PathVariable @NotNull Long eventId) {
        log.info("Creating chatroom for event with ID: {}", eventId);
        Chatroom chatroom = chatService.createChatRoomForEvent(eventId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ChatroomResponse(chatroom.getId()));
    }*/

    @GetMapping
    public ResponseEntity<ChatroomResponse> getChatroom(@PathVariable @NotNull Long eventId) {
        log.info("Getting chatroom for event with ID: {}", eventId);
        Chatroom chatroom = chatService.getChatroomByEventId(eventId);
        return ResponseEntity.ok(new ChatroomResponse(chatroom.getId()));
    }

    /*@DeleteMapping
    public ResponseEntity<Void> deleteChatroom(@PathVariable @NotNull Long eventId) {
        log.info("Deleting chatroom for event with ID: {}", eventId);
        chatService.deleteChatroomByEventId(eventId);
        return ResponseEntity.noContent().build();
    }*/

}
