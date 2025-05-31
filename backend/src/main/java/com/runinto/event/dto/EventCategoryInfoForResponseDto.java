package com.runinto.event.dto;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.dto.response.EventResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EventCategoryInfoForResponseDto {
    private String category;

    public EventCategoryInfoForResponseDto(String categoryName) {
        this.category = categoryName;
    }

    public static EventCategoryInfoForResponseDto from(final EventCategory eventCategory) {
        if (eventCategory == null || eventCategory.getCategory() == null) {
            return null; // 또는 기본값, 또는 예외 처리
        }
        // eventCategory.getCategory().name() 만 사용 (toString()은 불필요)
        return new EventCategoryInfoForResponseDto(eventCategory.getCategory().name());
    }

}
