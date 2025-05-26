package com.runinto.event.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.runinto.event.domain.EventType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.ErrorResponse;

import java.sql.Time;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEventRequestDto {
    @NotBlank(message = "이벤트 제목은 필수입니다.")
    @Size(max = 100, message = "이벤트 제목은 최대 100자까지 가능합니다.")
    private String title;

    @NotBlank(message = "이벤트 설명은 필수입니다.")
    @Size(max = 1000, message = "이벤트 설명은 최대 1000자까지 가능합니다.")
    private String description;

    @NotNull(message = "최대 참가자 수는 필수입니다.")
    @Min(value = 1, message = "최대 참가자 수는 최소 1명 이상이어야 합니다.")
    private Integer maxParticipants;

    @NotNull(message = "위도는 필수입니다.")
    @DecimalMin(value = "-90.0", message = "위도는 -90에서 90 사이의 값이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 -90에서 90 사이의 값이어야 합니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    @DecimalMin(value = "-180.0", message = "경도는 -180에서 180 사이의 값이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 -180에서 180 사이의 값이어야 합니다.")
    private Double longitude;

    @NotNull(message = "공개 여부는 필수입니다.")
    private Boolean isPublic;

    @NotEmpty(message = "이벤트 카테고리는 최소 하나 이상 선택해야 합니다.")
    private Set<EventType> categories;

    @NotNull(message = "생성 시간은 필수입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private Time creationTime;

}
