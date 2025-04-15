package com.runinto.chat.presntation;

import com.runinto.chat.domain.ChatMessage;
import com.runinto.chat.dto.request.ChatMessageResponse;
import com.runinto.chat.dto.response.ChatMessageRequest;
import com.runinto.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        //ChatMessage savedMessage = chatService.sendMessage(chatroomId, request);
        //return ResponseEntity.ok(ChatMessageResponse.from(savedMessage));
        return ResponseEntity.ok("메시지 전송 성공");
    }

    //메시지 받기 어떻게 할까..

}
