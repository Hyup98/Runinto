package com.runinto.chat.presntation;

import com.runinto.chat.dto.response.ChatroomResponse;
import com.runinto.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/chatrooms")
@RequiredArgsConstructor
public class ChatroomListController {
    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<List<ChatroomResponse>> getAllChatrooms() {
        List<ChatroomResponse> chatrooms = chatService.getAllChatrooms();
        return ResponseEntity.ok(chatrooms);
    }
}
