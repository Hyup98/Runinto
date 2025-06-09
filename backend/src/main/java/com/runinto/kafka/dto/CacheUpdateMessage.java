package com.runinto.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CacheUpdateMessage {
    private String action; // 예: "INVALIDATE_GRID"
    private String gridId; // 무효화할 그리드 ID
    // 필요하다면 다른 데이터 추가 가능
}