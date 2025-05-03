package com.runinto.user.domain.repository;

import com.runinto.event.domain.*;
import com.runinto.event.domain.repository.EventJpaRepository;
import com.runinto.user.domain.Gender;
import com.runinto.user.domain.Role;
import com.runinto.user.domain.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({UserH2Repository.class})
class UserH2RepositoryTest {

    @Autowired
    private UserH2Repository userH2Repository;

    @Autowired
    private EventJpaRepository eventJpaRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("tester")
                .email("tester@example.com")
                .password("password123")
                .imgUrl("profile.jpg")
                .description("test user")
                .gender(Gender.MALE)
                .age(30)
                .role(Role.USER)
                .eventParticipants(new HashSet<>()) // 초기에는 참여 없음
                .build();

        userH2Repository.save(user);
    }

    @AfterEach
    void tearDown() {
        userH2Repository.deleteAll();
    }

    @Test
    void findById_shouldReturnUser() {
        Optional<User> found = userH2Repository.findById(user.getUserId());
        assertTrue(found.isPresent());
        assertEquals("tester", found.get().getName());
        assertEquals("tester@example.com", found.get().getEmail());
    }

    @Test
    void save_shouldPersistNewUser() {
        User newUser = User.builder()
                .name("newuser")
                .email("new@example.com")
                .password("secure")
                .imgUrl("img.jpg")
                .description("새 사용자")
                .gender(Gender.FEMALE)
                .age(25)
                .role(Role.USER)
                .eventParticipants(new HashSet<>())
                .build();

        userH2Repository.save(newUser);

        Optional<User> saved = userH2Repository.findById(newUser.getUserId());
        assertTrue(saved.isPresent());
        assertEquals("new@example.com", saved.get().getEmail());
    }

    @Test
    void delete_shouldRemoveUser() {
        Long id = user.getUserId();
        userH2Repository.delete(id);
        assertFalse(userH2Repository.findById(id).isPresent());
    }

    @Test
    void delete_shouldNotThrow_whenUserDoesNotExist() {
        assertDoesNotThrow(() -> userH2Repository.delete(999L));
    }

    @Test
    void findJoinedEvents_shouldReturnAssociatedEvents() {
        Event event = Event.builder()
                .title("테스트 이벤트")
                .description("이벤트 설명")
                .latitude(37.5)
                .longitude(127.0)
                .creationTime(Time.valueOf(LocalTime.now()))
                .maxParticipants(5)
                .participants(1)
                .chatroomId(10L)
                .build();

        EventCategory category = EventCategory.builder()
                .category(EventType.TALKING)
                .event(event)
                .build();
        event.setEventCategories(new HashSet<>(Set.of(category)));

        EventParticipant participation = EventParticipant.builder()
                .user(user)
                .event(event)
                .status(ParticipationStatus.APPROVED)
                .appliedAt(LocalDateTime.now())
                .build();
        event.setEventParticipants(new HashSet<>(Set.of(participation)));
        user.getEventParticipants().add(participation);

        eventJpaRepository.save(event);
        userH2Repository.save(user);

        List<Event> events = userH2Repository.findJoinedEvents(user.getUserId());
        assertEquals(1, events.size());
        assertEquals("테스트 이벤트", events.get(0).getTitle());
    }

    @Test
    void findJoinedEvents_shouldReturnEmptyList_whenNoneExist() {
        List<Event> events = userH2Repository.findJoinedEvents(user.getUserId());
        assertTrue(events.isEmpty());
    }
}
