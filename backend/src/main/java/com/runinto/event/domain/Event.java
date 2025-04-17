package com.runinto.event.domain;

import com.runinto.chat.domain.Chatroom;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*todo
맴버 변수에 연령대 추가 -> 새로운 enum으로 10대 20대등
 */
@Data
@NoArgsConstructor
public class Event {
    private static final double EPSILON = 1e-9;

    private Long eventId;
    private String title;
    private String description;
    private int maxParticipants;
    private Time creationTime;
    private double latitude;
    private double longitude;
    private Long chatroomId;
    private boolean isPublic;
    private int participants;

    private Set<EventCategory> eventCategories = new HashSet<>();
    private List<Long> applicants = new ArrayList<>();

    @Builder
    public Event(String title, Long eventId, String description, int maxParticipants, Time creationTime, double latitude, double longitude, Long chatroomId, int participants, Set<EventCategory> categories) {
        this.title = title;
        this.eventId = eventId;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.creationTime = creationTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.chatroomId = chatroomId;
        this.eventCategories =categories;
        this.isPublic = true;
        this.participants = participants;
    }

    public void application(Long userId) {
        applicants.add(userId);
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean hasMatchingCategory(List<EventCategory> categorys) {
        Set<EventType> requestSet = categorys.stream()
                .map(EventCategory::getCategory)
                .collect(Collectors.toSet());
        return eventCategories.stream()
                .map(EventCategory::getCategory)
                .anyMatch(requestSet::contains);
    }

    public boolean isInArea(double nelat, double nelng, double swlat, double swlng) {
        return latitude >= swlat - EPSILON &&
                latitude <= nelat + EPSILON &&
                longitude >= swlng - EPSILON &&
                longitude <= nelng + EPSILON;
    }
}


