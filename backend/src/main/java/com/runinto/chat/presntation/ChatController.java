package com.runinto.chat.presntation;

import com.runinto.chat.domain.ChatMessage;
import com.runinto.chat.dto.request.ChatMessageResponse;
import com.runinto.chat.dto.response.ChatMessageRequest;
import com.runinto.chat.service.ChatService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.message.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;

@RequiredArgsConstructor
@RestController
@RequestMapping("chat")
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/{chatroomId}/messages")
    public ResponseEntity<String> sendMessage(
            @PathVariable Long chatroomId,
            @RequestBody ChatMessageRequest request
    ) {
        ChatMessage  chatMessage = ChatMessage.builder()
                .ChatRoomId(chatroomId)
                .senderId("user123")
                .message("안녕하세요!")
                .sendTime(new Time(System.currentTimeMillis()))
                .build();
        chatService.sendMessage(chatMessage);
        return ResponseEntity.ok("메시지 전송 성공");
    }

}
