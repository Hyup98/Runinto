package com.runinto.chat.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CustomWebSocketHandler extends TextWebSocketHandler {
    //지금 버전은 같은 유저id이면 여러 페이지에서 동시에 사용하면 마지막 접속한 페이지만 유효 -> 덮어써진다
    //이걸 응용해서 기기당 로그인등(모바일 하나, 데스크탑 하나 등)을 구현할 수 있겠다.
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override // 웹 소켓 연결시
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        String userId = (String) session.getAttributes().get("userId");

        if (userId != null) {
            sessions.put(Long.parseLong(userId), session);
            log.info("[연결됨] sessionId={}, userId={}", sessionId, userId);

            // 세션 속성에 명시적으로 저장
            session.getAttributes().put("sessionId", sessionId);
        } else {
            log.warn("userId가 누락되었습니다. 연결 거부 고려 필요");
        }
    }

    @Override // 데이터 통신시
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String sessionId = (String) session.getAttributes().get("sessionId");
        String payload = message.getPayload();

        log.info("[메시지 수신] userId={}, sessionId={}, message={}", userId, sessionId, payload);

        // 모든 접속한 세션에 메시지 브로드캐스트
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage("[유저 " + userId + "] " + payload));
                } catch (IOException e) {
                    log.warn("메시지 전송 실패: {}", e.getMessage());
                }
            }
        }
    }

    @Override // 웹소켓 통신 에러시
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }

    @Override // 웹 소켓 연결 종료시
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
    }
}