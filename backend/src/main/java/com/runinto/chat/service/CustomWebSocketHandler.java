package com.runinto.chat.service;

import com.runinto.chat.domain.repository.chatroom.ChatroomParticipant;
import com.runinto.chat.proto.ChatMessageProto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CustomWebSocketHandler extends BinaryWebSocketHandler {
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ChatService chatService;

    public CustomWebSocketHandler(ChatService chatService) {
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
            session.close(CloseStatus.POLICY_VIOLATION.withReason("userId is required"));
        }
    }

    @Override// 데이터 통신시
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        ByteBuffer byteBuffer = message.getPayload();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes); // ByteBuffer에서 byte[] 추출

        // Protobuf 역직렬화
        ChatMessageProto.ChatMessage chatMessageProto;
        try {
            chatMessageProto = ChatMessageProto.ChatMessage.parseFrom(bytes);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            log.warn("잘못된 Protobuf 메시지 수신: {}. 세션 ID: {}", e.getMessage(), session.getId());
            session.close(CloseStatus.BAD_DATA.withReason("Invalid Protobuf message"));
            return;
        }

        // Protobuf 객체에서 데이터 추출 (getChatRoomId()는 long 반환)
        long chatroomId = chatMessageProto.getChatRoomId();
        long senderId = chatMessageProto.getSenderId();
        String content = chatMessageProto.getMessage();

        log.info("[Protobuf 메시지 수신] userId={}, chatroomId={}, message={}", senderId, chatroomId, content);

        Set<ChatroomParticipant> participants = chatService.findChatroomParticipantByChatroomID(chatroomId).orElse(null);
        log.info("참여자 수 {}", participants == null ? 0 : participants.size() );
        if(participants == null || participants.isEmpty()) {
            log.warn("채팅방 ID {}에 참여자가 없습니다.", chatroomId);
            return;
        }

        for (ChatroomParticipant participant : participants) {
            Long participantUserId = participant.getId(); // 실제 유저 ID를 가져오는지 확인 필요
            WebSocketSession participantSession = sessions.get(participantUserId);

            if (participantSession != null && participantSession.isOpen()) {
                try {
                    // Protobuf 메시지 객체 생성 (응답용)
                    ChatMessageProto.ChatMessage messageToSendProto = ChatMessageProto.ChatMessage.newBuilder()
                            .setChatRoomId(chatroomId)
                            .setSenderId(senderId)
                            .setMessage(content)
                            .build();

                    // Protobuf 객체를 byte[]로 직렬화
                    byte[] outputBytes = messageToSendProto.toByteArray();
                    participantSession.sendMessage(new BinaryMessage(outputBytes));
                    log.info("[Protobuf 메시지 전송 완료] toParticipantId={}, message={}", participantUserId, content);
                } catch (IOException e) {
                    log.warn("메시지 전송 실패: toParticipantId={}, error={}", participantUserId, e.getMessage());
                }
            } else {
                log.warn("참여자 {}의 세션이 존재하지 않거나 닫혀있습니다. 메시지 전송 스킵.", participantUserId);
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