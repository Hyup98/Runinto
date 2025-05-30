package com.runinto.chat.service; // 또는 원래 사용하시던 패키지 경로

// ObjectMapper 및 MessagePack 관련 import 제거
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.msgpack.jackson.dataformat.MessagePackFactory;

// 기존 DTO 대신 Protobuf 생성 클래스 사용
// import com.runinto.chat.dto.request.ChatMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runinto.chat.dto.request.ChatMessageRequest;
import com.runinto.chat.proto.ChatMessageProto; // 생성된 Protobuf 클래스 임포트

import lombok.extern.slf4j.Slf4j;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
@Component
public class TestCustomWebSocketHandler extends BinaryWebSocketHandler {
    // ObjectMapper 필드 제거 (Protobuf는 자체 직렬화/역직렬화 사용)
    // private final ObjectMapper objectMapper;

    // ChatService 의존성 제거된 상태 유지
    public TestCustomWebSocketHandler() {
        // ObjectMapper 초기화 코드 제거
        log.info("Simplified TestCustomWebSocketHandler (Protobuf Echo) for performance testing initialized.");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        // 로그 메시지 변경
        log.info("[Protobuf Echo Test] Connection established: sessionId={}", sessionId);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        ByteBuffer byteBuffer = message.getPayload();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);

        // Protobuf 역직렬화
        ChatMessageProto.ChatMessage receivedMessage;
        try {
            receivedMessage = ChatMessageProto.ChatMessage.parseFrom(bytes);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            log.warn("[Protobuf Echo Test] Invalid Protobuf message received: {}. Session ID: {}", e.getMessage(), session.getId());
            if (session.isOpen()) {
                session.close(CloseStatus.BAD_DATA.withReason("Invalid Protobuf message"));
            }
            return;
        }

        // Protobuf 객체에서 데이터 추출
        long originalSenderId = receivedMessage.getSenderId();
        String originalContent = receivedMessage.getMessage();
        // long originalChatRoomId = receivedMessage.getChatRoomId(); // 에코 테스트에서는 무시 가능

        log.info("[Protobuf Echo Test] Message received from sessionID={}, senderId={}, content='{}'",
                session.getId(), originalSenderId, originalContent);

        // 에코 메시지 생성 (Protobuf 객체 사용)
        ChatMessageProto.ChatMessage echoResponseMessage = ChatMessageProto.ChatMessage.newBuilder()
                .setSenderId(originalSenderId) // 원래 senderId 또는 서버 ID (예: 0L)
                .setMessage("Echo from server: " + originalContent)
                .setChatRoomId(0L) // 채팅방 ID는 에코에서 중요하지 않으므로 임의의 값
                .build();

        try {
            // Protobuf 객체를 byte[]로 직렬화
            byte[] outputBytes = echoResponseMessage.toByteArray();
            if (session.isOpen()) {
                session.sendMessage(new BinaryMessage(outputBytes));
                log.info("[Protobuf Echo Test] Echo sent back to sessionID={}", session.getId());
            } else {
                log.warn("[Protobuf Echo Test] Session {} is not open. Cannot send echo.", session.getId());
            }
        } catch (IOException e) {
            log.warn("[Protobuf Echo Test] Failed to send echo message to sessionID={}, error={}", session.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 로그 메시지 변경
        log.error("[Protobuf Echo Test] WebSocket transport error for sessionID={}: {}", session.getId(), exception.getMessage(), exception);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR.withReason(exception.getMessage()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 로그 메시지 변경
        log.info("[Protobuf Echo Test] Connection closed for sessionID={}, status={}", session.getId(), status);
    }

   /* private final ObjectMapper objectMapper; // MessagePack용 ObjectMapper 필드

    public TestCustomWebSocketHandler() {
        // MessagePackFactory를 사용하는 ObjectMapper 초기화
        this.objectMapper = new ObjectMapper(new MessagePackFactory());
        log.info("Simplified TestCustomWebSocketHandler (MessagePack Echo) for performance testing initialized.");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        // 로그 메시지 변경
        log.info("[MessagePack Echo Test] Connection established: sessionId={}", sessionId);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        ByteBuffer byteBuffer = message.getPayload();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);

        // MessagePack 역직렬화 (ChatMessageRequest DTO 사용)
        ChatMessageRequest receivedMessage;
        try {
            receivedMessage = objectMapper.readValue(bytes, ChatMessageRequest.class);
        } catch (IOException e) {
            log.warn("[MessagePack Echo Test] Invalid MessagePack message received: {}. Session ID: {}", e.getMessage(), session.getId());
            if (session.isOpen()) {
                session.close(CloseStatus.BAD_DATA.withReason("Invalid MessagePack message"));
            }
            return;
        }

        // DTO에서 데이터 추출
        Long originalSenderId = receivedMessage.getSenderId();
        String originalContent = receivedMessage.getMessage();
        // Long originalChatRoomId = receivedMessage.getChatRoomId(); // 에코 테스트에서는 무시 가능

        log.info("[MessagePack Echo Test] Message received from sessionID={}, senderId={}, content='{}'",
                session.getId(), originalSenderId, originalContent);

        // 에코 메시지 생성 (ChatMessageRequest DTO 사용)
        // ChatMessageRequest에 모든 필드를 받는 생성자 또는 setter가 있다고 가정합니다.
        ChatMessageRequest echoResponseMessage = new ChatMessageRequest();
        echoResponseMessage.setSenderId(originalSenderId); // 원래 senderId 또는 서버 ID (예: 0L)
        echoResponseMessage.setMessage("Echo from server: " + originalContent);
        echoResponseMessage.setChatRoomId(0L); // 채팅방 ID는 에코에서 중요하지 않으므로 임의의 값
        // 또는 모든 필드를 받는 생성자가 있다면:
        // ChatMessageRequest echoResponseMessage = new ChatMessageRequest(0L, originalSenderId, "Echo from server: " + originalContent);


        try {
            // ChatMessageRequest DTO를 MessagePack byte[]로 직렬화
            byte[] outputBytes = objectMapper.writeValueAsBytes(echoResponseMessage);
            if (session.isOpen()) {
                session.sendMessage(new BinaryMessage(outputBytes));
                log.info("[MessagePack Echo Test] Echo sent back to sessionID={}", session.getId());
            } else {
                log.warn("[MessagePack Echo Test] Session {} is not open. Cannot send echo.", session.getId());
            }
        } catch (IOException e) {
            log.warn("[MessagePack Echo Test] Failed to send echo message to sessionID={}, error={}", session.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 로그 메시지 변경
        log.error("[MessagePack Echo Test] WebSocket transport error for sessionID={}: {}", session.getId(), exception.getMessage(), exception);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR.withReason(exception.getMessage()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 로그 메시지 변경
        log.info("[MessagePack Echo Test] Connection closed for sessionID={}, status={}", session.getId(), status);
    }*/
}