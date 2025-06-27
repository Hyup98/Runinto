package common.kafka.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessageDto {
    private Long chatroomId;
    private Long senderId;
    private String senderName;
    private String senderImgUrl;
    private String content;
}