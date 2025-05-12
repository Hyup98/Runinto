package com.runinto.user.domain;

import com.runinto.chat.domain.repository.chatroom.ChatroomParticipant;
import com.runinto.event.domain.EventParticipant;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String imgUrl;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventParticipant> eventParticipants = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<ChatroomParticipant> chatParticipations = new HashSet<>();

    @Builder
    public User(Long userId, String name, String email, String password, String imgUrl, String description, Gender gender, Integer age, Role role, Set<EventParticipant> eventParticipants, Set<ChatroomParticipant> chatParticipations)  {
        this.userId = userId;
        this.name = name;
        this.imgUrl = imgUrl;
        this.description = description;
        this.gender = gender;
        this.age = age;
        this.email = email;
        this.password = password;
        this.role = role;
        this.eventParticipants = eventParticipants != null ? eventParticipants : new HashSet<>();;
        this.chatParticipations = chatParticipations != null ? chatParticipations : new HashSet<>();;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return userId != null && userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }
}




