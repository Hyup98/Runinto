package com.runinto.event.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.runinto.chat.domain.repository.chatroom.Chatroom;
import com.runinto.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/*todo
맴버 변수에 연령대 추가 -> 새로운 enum으로 10대 20대등
 */

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "event")
public class Event {
    private static final double EPSILON = 1e-9;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "creation_time")
    private Time creationTime;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    private int participants;

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Chatroom chatroom;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventCategory> eventCategories = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventParticipant> eventParticipants = new HashSet<>();

    @Builder
    public Event(String title, Long id, String description, int maxParticipants, Time creationTime, double latitude, double longitude, Chatroom chatroom, int participants, Set<EventCategory> categories) {
        this.title = title;
        this.id = id;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.creationTime = creationTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.chatroom = chatroom;
        this.eventCategories =categories;
        this.participants = participants;
    }

    public void application(User user) {
        EventParticipant participant = EventParticipant.builder()
                .event(this)
                .user(user)
                .appliedAt(LocalDateTime.now())
                .status(ParticipationStatus.REQUESTED)
                .build();

        this.eventParticipants.add(participant);
        user.getEventParticipants().add(participant);
    }

    public boolean hasMatchingCategory(Set<EventType> categorys) {
        return eventCategories.stream()
                .map(EventCategory::getCategory)
                .anyMatch(categorys::contains);
    }

    public boolean isInArea(double nelat, double nelng, double swlat, double swlng) {
        return latitude >= swlat - EPSILON &&
                latitude <= nelat + EPSILON &&
                longitude >= swlng - EPSILON &&
                longitude <= nelng + EPSILON;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event event)) return false;
        return id != null && id.equals(event.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
