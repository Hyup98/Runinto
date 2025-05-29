package com.runinto.chat.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runinto.chat.domain.repository.chatroom.Chatroom;
import com.runinto.chat.domain.repository.chatroom.ChatroomH2Repository;
import com.runinto.chat.domain.repository.chatroom.ChatroomParticipant;
import com.runinto.chat.dto.request.ChatMessageRequest;
import com.runinto.chat.service.ChatService;
import com.runinto.event.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CustomWebSocketHandler extends TextWebSocketHandler {
    //지금 버전은 같은 유저id이면 여러 페이지에서 동시에 사용하면 마지막 접속한 페이지만 유효 -> 덮어써진다
    //이걸 응용해서 기기당 로그인등(모바일 하나, 데스크탑 하나 등)을 구현할 수 있겠다.
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final ChatService chatService;

    private final ObjectMapper objectMapper;

    public CustomWebSocketHandler(ObjectMapper objectMapper, ChatService chatService) {
        this.objectMapper = objectMapper;
        this.chatService = chatService;
    }

    @Override // 웹 소켓 연결시
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        String userIdString = (String) session.getAttributes().get("userId");


        if (userIdString != null && !userIdString.isEmpty()) {
            try {
                Long userId = Long.parseLong(userIdString);
                sessions.put(userId, session);
                log.info("[연결됨] sessionId={}, userId={}", sessionId, userId);

                // 세션 속성에 명시적으로 저장
                session.getAttributes().put("sessionId", sessionId);
            } catch (NumberFormatException e) {
                log.warn("userId 파싱 실패: {}. 연결을 진행하지 않습니다.", userIdString, e);
                session.close(CloseStatus.BAD_DATA.withReason("Invalid userId format"));
            }
        } else {
            log.warn("userId가 누락되었습니다. 연결 거부 고려 필요. SessionId: {}", sessionId);
        }
    }

    @Override // 데이터 통신시
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatMessageRequest chatMessageRequest = objectMapper.readValue(message.getPayload(), ChatMessageRequest.class);

        Long chatroomId = chatMessageRequest.getChatRoomId();
        Long senderId = chatMessageRequest.getSenderId();
        String content = chatMessageRequest.getMessage();

        log.info("[메시지 수신] userId={}, chatroomId={}, message={}", senderId, chatroomId, content);

        Set<ChatroomParticipant> participants = chatService.findChatroomParticipantByChatroomID(chatroomId).orElse(null);
        log.info("참여자 수 {}", participants == null ? 0 : participants.size() );
        if(participants == null || participants.isEmpty()) {
            log.warn("채팅방 ID {}에 참여자가 없습니다.", chatroomId);
            return;
        }

        for(ChatroomParticipant participant : participants) {
            Long participantId = participant.getId(); // ChatroomParticipant에서 실제 사용자 ID를 가져오는 방식에 따라 수정 필요
            WebSocketSession participantSession = sessions.get(participantId);

            if (participantSession != null && participantSession.isOpen()) {
                try {
                    // 메시지 형식은 필요에 따라 수정 (예: JSON 형태의 메시지 객체)
                    String messageToSend = String.format("{\"senderId\": %d, \"message\": \"%s\", \"chatroomId\": %d}", senderId, content, chatroomId);
                    participantSession.sendMessage(new TextMessage(messageToSend));
                    log.info("[메시지 전송 완료] toParticipantId={}, message={}", participantId, content);
                } catch (IOException e) {
                    log.warn("메시지 전송 실패: toParticipantId={}, error={}", participantId, e.getMessage());
                }
            } else {
                log.warn("참여자 {}의 세션이 존재하지 않거나 닫혀있습니다. 메시지 전송 스킵.", participantId);
                // 필요하다면 여기서 sessions 맵에서 해당 participantId를 제거하는 로직을 추가할 수 있습니다.
                // sessions.remove(participantId);
            }
        }
    }

    @Override // 웹소켓 통신 에러시
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("웹소켓 통신 오류 발생: sessionId={}, error={}", session.getId(), exception.getMessage(), exception);
        session.getAttributes().get("userId");
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(Long.parseLong(userId));
            log.info("오류 발생으로 세션 제거: userId={}", userId);
        }
        super.handleTransportError(session, exception);
    }

    @Override // 웹 소켓 연결 종료시
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        // 연결 종료 시에도 session attributes에서 userId를 가져와야 합니다.
        String userIdString = (String) session.getAttributes().get("userId");

        if (userIdString != null && !userIdString.isEmpty()) {
            try {
                Long userId = Long.parseLong(userIdString);
                // 연결 종료 시, 해당 사용자의 세션이 현재 세션과 동일한 경우에만 제거
                WebSocketSession storedSession = sessions.get(userId);
                if (storedSession != null && storedSession.getId().equals(sessionId)) {
                    sessions.remove(userId);
                    log.info("[연결 종료] sessionId={}, userId={}, status={}", sessionId, userId, status);
                } else {
                    log.info("[연결 종료 - 세션 불일치 또는 없음] sessionId={}, userId={}, status={}. 저장된 세션 ID: {}",
                             sessionId, userId, status, storedSession != null ? storedSession.getId() : "null");
                }
            } catch (NumberFormatException e) {
                log.warn("연결 종료 처리 중 userId 파싱 실패: {}. SessionId: {}", userIdString, sessionId, e);
            }
        } else {
            log.info("[연결 종료 - userId 없음] sessionId={}, status={}", sessionId, status);
        }
        super.afterConnectionClosed(session, status);
    }
}