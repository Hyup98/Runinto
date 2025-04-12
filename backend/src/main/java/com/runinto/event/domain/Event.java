package com.runinto.event.domain;

import com.runinto.chat.domain.Chatroom;
import lombok.Builder;
import lombok.Data;

import java.sql.Time;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*todo
맴버 변수에 연령대 추가 -> 새로운 enum으로 10대 20대등
 */
@Data
public class Event {
    private Long eventId;
    private String title;
    private String description;
    private int maxParticipants;
    private Time creationTime;
    private double latitude;
    private double longitude;
    private Chatroom chatroom;
    private boolean isPublic;
    private int participants;

    private Set<EventCategory> eventCategories;

    private List<Long> applicants;

    @Builder
    public Event(String name, String description, int maxParticipants, Time creationTime, double latitude, double longitude, Chatroom chatroom, int participants) {
        this.title = name;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.creationTime = creationTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.chatroom = chatroom;
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
        Set<EventCategory> requestSet = new HashSet<>(categorys);
        return eventCategories.stream().anyMatch(requestSet::contains);
    }

    public boolean isInArea(double nelatitude, double nelongitude, double swlatitude, double swlongitude) {
        return latitude >= nelatitude &&
               latitude <= swlatitude &&
               longitude >= nelongitude &&
               longitude <= swlongitude;
    }
}


