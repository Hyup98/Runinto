package common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CacheUpdateMessage {
    private String action; // 수행할 작업 (예: "INVALIDATE_GRID")
    private String gridId; // 작업 대상이 되는 그리드 ID
}