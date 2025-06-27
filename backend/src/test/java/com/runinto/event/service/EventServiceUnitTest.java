package com.runinto.event.service;

import com.runinto.chat.domain.repository.chatroom.Chatroom;
import com.runinto.chat.domain.repository.chatroom.ChatroomH2Repository; // 실제 Chatroom 리포지토리 (사용되지 않음)
import com.runinto.chat.domain.repository.chatroom.ChatroomParticipant;
import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;
import com.runinto.event.domain.EventParticipant;
import com.runinto.event.domain.ParticipationStatus;
import com.runinto.event.domain.repository.EventH2Repository;
import com.runinto.exception.event.EventNotFoundException;
import com.runinto.exception.user.UserIdNotFoundException;
import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserH2Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService 단위 테스트")
class EventServiceUnitTest {

    @Mock
    private EventH2Repository eventRepositoryMock;

    @Mock
    private UserH2Repository userRepositoryMock;

    @Mock
    private ChatroomH2Repository chatroomRepositoryMock; // 생성자에만 사용, 실제 로직에서 직접 호출 X

    @InjectMocks
    private EventService eventService;

    private User eventCreator, testUser1, testUser2;
    private Event testEvent1, testEventWithNoChatroom;
    private Chatroom testChatroomForEvent1;

    // Event.java의 빌더 시그니처: (String title, Long id, String description, int maxParticipants, Time creationTime, double latitude, double longitude, Chatroom chatroom, int participants, Set<EventCategory> categories)
    private Event buildEventForTest(Long id, String title, String description, int maxParticipants, Time creationTime,
                                    double latitude, double longitude, boolean isPublicFlag, int initialParticipantCount,
                                    Chatroom chatroom, Set<EventCategory> categoriesSet) {
        Event event = Event.builder()
                .id(id)
                .title(title)
                .description(description)
                .maxParticipants(maxParticipants)
                .creationTime(creationTime)
                .latitude(latitude)
                .longitude(longitude)
                .chatroom(chatroom)
                .categories(categoriesSet != null ? categoriesSet : new HashSet<>())
                .build();
        event.setPublic(isPublicFlag);
        event.setEventParticipants(new HashSet<>()); // EventParticipant 목록은 수동으로 관리
        return event;
    }

    private void addCategoryToEvent(Event event, EventType eventType) {
        EventCategory category = EventCategory.builder().category(eventType).event(event).build();
        if (event.getEventCategories() == null) event.setEventCategories(new HashSet<>());
        event.getEventCategories().add(category);
    }

    private EventParticipant addManagerToEvent(Event event, User managerUser) {
        EventParticipant managerParticipant = EventParticipant.builder()
                .event(event).user(managerUser).status(ParticipationStatus.MANAGER).appliedAt(LocalDateTime.now()).build();
        if (event.getEventParticipants() == null) event.setEventParticipants(new HashSet<>());
        event.getEventParticipants().add(managerParticipant);
        if (managerUser.getEventParticipants() == null) managerUser.setEventParticipants(new HashSet<>());
        managerUser.getEventParticipants().add(managerParticipant);
        return managerParticipant;
    }

    @BeforeEach
    void setUp() {
        eventCreator = User.builder().userId(1L).name("이벤트생성자").email("creator@example.com").eventParticipants(new HashSet<>()).chatParticipations(new HashSet<>()).build();
        testUser1 = User.builder().userId(2L).name("테스터1").email("tester1@example.com").eventParticipants(new HashSet<>()).chatParticipations(new HashSet<>()).build();
        testUser2 = User.builder().userId(3L).name("테스터2").email("tester2@example.com").eventParticipants(new HashSet<>()).chatParticipations(new HashSet<>()).build();

        testEvent1 = buildEventForTest(101L, "테스트 이벤트 1", "설명 for 테스트 이벤트 1", 10,
                Time.valueOf(LocalTime.now()), 37.565, 127.015, true, 0,
                null, new HashSet<>());
        addCategoryToEvent(testEvent1, EventType.ACTIVITY);
        addManagerToEvent(testEvent1, eventCreator);

        testChatroomForEvent1 = Chatroom.builder().event(testEvent1).participants(new HashSet<>()).build();
        testEvent1.setChatroom(testChatroomForEvent1);

        testEventWithNoChatroom = buildEventForTest(102L, "채팅방 없는 이벤트", "설명 for 채팅방 없는 이벤트", 5,
                Time.valueOf(LocalTime.now()), 37.600, 127.020, true, 0,
                null, new HashSet<>());
        addCategoryToEvent(testEventWithNoChatroom, EventType.TALKING);
        // 이 이벤트는 매니저를 명시적으로 추가하지 않음 (createEventWithChatroom 테스트에서 생성자가 매니저로 추가됨)
    }

    @Nested
    @DisplayName("findById 메소드는")
    class Describe_findById {
        @Test
        @DisplayName("존재하는 ID로 조회 시 해당 이벤트를 반환한다")
        void whenIdExists_returnsEvent() {
            when(eventRepositoryMock.findById(testEvent1.getId())).thenReturn(Optional.of(testEvent1));
            Event foundEvent = eventService.findById(testEvent1.getId());
            assertThat(foundEvent).isEqualTo(testEvent1);
            verify(eventRepositoryMock).findById(testEvent1.getId());
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 ResponseStatusException (NOT_FOUND)을 던진다")
        void whenIdNotExists_throwsResponseStatusException() {
            long nonExistentId = 99L;
            when(eventRepositoryMock.findById(nonExistentId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> eventService.findById(nonExistentId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("이벤트를 찾을 수 없습니다.")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
            verify(eventRepositoryMock).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("save 메소드는 (@Transactional)")
    class Describe_save {
        @Test
        @DisplayName("주어진 Event 객체로 EventH2Repository.save를 호출한다")
        void whenEventGiven_callsRepositorySave() {
            // EventService.save는 void이고 @Transactional이므로,
            // 주로 다른 메소드의 일부로 테스트되거나, 호출 여부만 검증합니다.
            // eventRepositoryMock.save는 Event를 반환하도록 설정 (EventH2Repository.save 시그니처)
            when(eventRepositoryMock.save(testEvent1)).thenReturn(testEvent1);

            eventService.save(testEvent1);

            verify(eventRepositoryMock).save(testEvent1);
        }
    }

    @Nested
    @DisplayName("createEventWithChatroom 메소드는 (@Transactional)")
    class Describe_createEventWithChatroom {
        @Test
        @DisplayName("유효한 이벤트와 사용자 정보로 호출 시, 이벤트를 저장하고, 채팅방을 생성하여 연결하며, 사용자를 매니저로 참여시킨 후 저장된 이벤트를 반환한다")
        void whenValidEventAndUser_savesEvent_createsAndLinksChatroom_addsUserAsManager_returnsSavedEvent() {
            Event eventToCreate = buildEventForTest(null, "새 이벤트와 채팅방", "설명", 5,
                    Time.valueOf(LocalTime.now()), 37.1, 127.1, true, 0, null, new HashSet<>());
            addCategoryToEvent(eventToCreate, EventType.TALKING);
            // eventToCreate.setEventParticipants(new HashSet<>()); // 빌더에서 초기화됨

            when(userRepositoryMock.existsByUserId(eventCreator.getUserId())).thenReturn(true);
            // eventRepository.save는 ID가 할당된 Event 객체를 반환하도록 모킹
            when(eventRepositoryMock.save(any(Event.class))).thenAnswer(invocation -> {
                Event arg = invocation.getArgument(0);
                // 실제 DB처럼 ID를 할당하는 것을 시뮬레이션 (테스트의 명확성을 위해)
                if (arg.getId() == null) {
                    arg.setId(105L); // 임의의 ID
                }
                // 서비스 로직에서 Chatroom이 생성되고 Event에 setChatroom 될 것임
                return arg;
            });

            Event result = eventService.createEventWithChatroom(eventToCreate, eventCreator);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(105L); // 모킹된 ID 확인
            assertThat(result.getTitle()).isEqualTo("새 이벤트와 채팅방");

            // Chatroom 생성 및 연결 검증
            assertThat(result.getChatroom()).isNotNull();
            assertThat(result.getChatroom().getEvent()).isEqualTo(result); // 양방향 관계

            // 생성자가 매니저로 참여했는지 검증
            assertThat(result.getEventParticipants()).hasSize(1);
            EventParticipant managerParticipant = result.getEventParticipants().iterator().next();
            assertThat(managerParticipant.getUser()).isEqualTo(eventCreator);
            assertThat(managerParticipant.getStatus()).isEqualTo(ParticipationStatus.MANAGER);
            assertThat(managerParticipant.getEvent()).isEqualTo(result); // 양방향

            // User 엔티티에도 EventParticipant가 추가되었는지 확인
            assertThat(eventCreator.getEventParticipants()).contains(managerParticipant);

            verify(userRepositoryMock).existsByUserId(eventCreator.getUserId());
            verify(eventRepositoryMock).save(eventToCreate); // 저장된 eventToCreate 객체 확인
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 호출 시 UserIdNotFoundException을 던진다")
        void whenUserNotExists_throwsUserIdNotFoundException() {
            User nonExistentUser = User.builder().userId(99L).build();
            Event event = buildEventForTest(null, "이벤트", "설명", 10, Time.valueOf(LocalTime.now()),
                    37.0, 127.0, true, 0, null, null);
            when(userRepositoryMock.existsByUserId(nonExistentUser.getUserId())).thenReturn(false);

            assertThatThrownBy(() -> eventService.createEventWithChatroom(event, nonExistentUser))
                    .isInstanceOf(UserIdNotFoundException.class)
                    .hasMessageContaining("User id not found: " + nonExistentUser.getUserId() + " .");
            verify(userRepositoryMock).existsByUserId(nonExistentUser.getUserId());
            verify(eventRepositoryMock, never()).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("delete 메소드는 (@Transactional)")
    class Describe_delete {
        @Test
        @DisplayName("존재하는 이벤트 ID로 호출 시, 이벤트를 찾아 EventH2Repository.delete를 호출하고 true를 반환한다")
        void whenEventExists_findsAndCallsRepositoryDelete_returnsTrue() {
            when(eventRepositoryMock.findById(testEvent1.getId())).thenReturn(Optional.of(testEvent1));
            when(eventRepositoryMock.delete(testEvent1)).thenReturn(true); // EventH2Repository.delete(Event)는 boolean 반환

            boolean result = eventService.deleteEvent(testEvent1.getId(), );

            assertThat(result).isTrue();
            verify(eventRepositoryMock).findById(testEvent1.getId());
            verify(eventRepositoryMock).delete(testEvent1);
        }

        @Test
        @DisplayName("존재하지 않는 이벤트 ID로 호출 시 EventNotFoundException을 던진다")
        void whenEventNotExists_throwsEventNotFoundException() {
            long nonExistentId = 99L;
            when(eventRepositoryMock.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.deleteEvent(nonExistentId, ))
                    .isInstanceOf(EventNotFoundException.class)
                    .hasMessageContaining("Event not found"); // EventService.delete의 예외 메시지
            verify(eventRepositoryMock).findById(nonExistentId);
            verify(eventRepositoryMock, never()).delete(any(Event.class));
        }
    }


    @Nested
    @DisplayName("approveParticipant 메소드는 (@Transactional)")
    class Describe_approveParticipant {
        private EventParticipant requestedParticipant;

        @BeforeEach
        void setUpApprove() {
            // testEvent1에는 eventCreator가 매니저로 이미 참여 중
            // testUser1이 참여 요청한 상태로 설정
            requestedParticipant = EventParticipant.builder()
                    .id(301L) // 테스트용 ID
                    .user(testUser1)
                    .event(testEvent1)
                    .status(ParticipationStatus.REQUESTED)
                    .appliedAt(LocalDateTime.now())
                    .build();
            testEvent1.getEventParticipants().add(requestedParticipant);
            testUser1.getEventParticipants().add(requestedParticipant); // 양방향

            // Chatroom 설정 (EventService 로직에 필요)
            testChatroomForEvent1.getParticipants().clear(); // 채팅방 참여자 초기화
            testEvent1.setChatroom(testChatroomForEvent1);

            when(eventRepositoryMock.findById(testEvent1.getId())).thenReturn(Optional.of(testEvent1));
        }

        @Test
        @DisplayName("요청 상태의 참여자를 승인하면, 상태가 APPROVED로 변경되고 채팅방에 해당 사용자가 추가된다")
        void whenParticipantIsRequested_approvesAndAddsUserToChatroom() {
            eventService.approveParticipant(testEvent1.getId(), testUser1.getUserId(), testUser1.getUserId());

            assertThat(requestedParticipant.getStatus()).isEqualTo(ParticipationStatus.APPROVED);

            // ChatroomParticipant가 생성되어 Chatroom과 User에 추가되었는지 확인
            assertThat(testEvent1.getChatroom().getParticipants()).hasSize(1);
            ChatroomParticipant chatParticipant = testEvent1.getChatroom().getParticipants().iterator().next();
            assertThat(chatParticipant.getUser()).isEqualTo(testUser1);
            assertThat(chatParticipant.getChatroom()).isEqualTo(testEvent1.getChatroom());

            assertThat(testUser1.getChatParticipations()).hasSize(1);
            assertThat(testUser1.getChatParticipations().iterator().next()).isEqualTo(chatParticipant);

            verify(eventRepositoryMock).findById(testEvent1.getId());
        }

        @Test
        @DisplayName("이벤트에 채팅방이 없으면 IllegalStateException을 던진다 ('No chatroom found for event')")
        void whenEventHasNoChatroom_throwsIllegalStateException() {
            testEvent1.setChatroom(null); // 채팅방 제거
            assertThatThrownBy(() -> eventService.approveParticipant(testEvent1.getId(), testUser1.getUserId(), testUser1.getUserId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No chatroom found for event " + testEvent1.getId());
        }

        @Test
        @DisplayName("참여자가 REQUESTED 상태가 아니면 IllegalStateException을 던진다 ('Only REQUESTED participants can be approved')")
        void whenParticipantNotRequested_throwsIllegalStateException() {
            requestedParticipant.setStatus(ParticipationStatus.APPROVED); // 이미 승인된 상태로 변경
            assertThatThrownBy(() -> eventService.approveParticipant(testEvent1.getId(), testUser1.getUserId(), testUser1.getUserId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Participant is already approved.");
        }

        @Test
        @DisplayName("승인하려는 참여자가 이벤트에 없으면 EventNotFoundException을 던진다 ('EventParticipant not found')")
        void whenApprovingNonExistentParticipant_throwsEventNotFoundException() {
            long nonExistentParticipantUserId = 999L;
            assertThatThrownBy(() -> eventService.approveParticipant(testEvent1.getId(), nonExistentParticipantUserId))
                    .isInstanceOf(EventNotFoundException.class)
                    .hasMessageContaining("EventParticipant not found for userId = " + nonExistentParticipantUserId);
        }
    }

    @Nested
    @DisplayName("appliyToEvent 메소드는 (@Transactional)")
    class Describe_appliyToEvent {
        @BeforeEach
        void setUpAppliy() {
            // 매니저(eventCreator)만 참여한 상태로 testEvent1 설정
            testEvent1.getEventParticipants().clear();
            addManagerToEvent(testEvent1, eventCreator);
            testChatroomForEvent1.getParticipants().clear(); // 채팅방 참여자 비움

            when(eventRepositoryMock.findById(testEvent1.getId())).thenReturn(Optional.of(testEvent1));
        }

        @Test
        @DisplayName("정상적인 경우, 이벤트 참여 신청이 성공하고 참여자 상태는 REQUESTED가 되며, Event와 User 양쪽에 EventParticipant가 추가된다")
        void whenNormalCase_appliesSuccessfully_statusIsRequested_andParticipantAddedToBothSides() {
            when(userRepositoryMock.findById(testUser1.getUserId())).thenReturn(Optional.of(testUser1));

            eventService.appliyToEvent(testEvent1.getId(), testUser1.getUserId());

            // Event 객체 내의 eventParticipants 컬렉션 확인
            assertThat(testEvent1.getEventParticipants())
                    .filteredOn(ep -> ep.getUser().equals(testUser1))
                    .hasSize(1)
                    .first()
                    .satisfies(participant -> {
                        assertThat(participant.getEvent()).isEqualTo(testEvent1);
                        assertThat(participant.getStatus()).isEqualTo(ParticipationStatus.REQUESTED);
                        assertThat(participant.getAppliedAt()).isNotNull();
                    });

            // User 객체 내의 eventParticipants 컬렉션 확인 (양방향)
            assertThat(testUser1.getEventParticipants())
                    .filteredOn(ep -> ep.getEvent().equals(testEvent1))
                    .hasSize(1)
                    .first()
                    .satisfies(participant -> {
                        assertThat(participant.getUser()).isEqualTo(testUser1);
                        assertThat(participant.getStatus()).isEqualTo(ParticipationStatus.REQUESTED);
                    });

            verify(eventRepositoryMock).findById(testEvent1.getId());
            verify(userRepositoryMock).findById(testUser1.getUserId());
        }

        @Test
        @DisplayName("이미 신청했거나 참여 중인 사용자가 다시 신청하면 IllegalStateException을 던진다 ('User already applied or is participating')")
        void whenUserAlreadyAppliedOrParticipating_throwsIllegalStateException() {
            // Given: testUser1이 이미 testEvent1에 REQUESTED 상태로 참여 신청 중이라고 설정
            EventParticipant existingParticipant = EventParticipant.builder()
                    .user(testUser1).event(testEvent1).status(ParticipationStatus.REQUESTED).appliedAt(LocalDateTime.now().minusDays(1)).build();
            testEvent1.getEventParticipants().add(existingParticipant);
            testUser1.getEventParticipants().add(existingParticipant);

            // when(userRepositoryMock.findById(testUser1.getUserId())).thenReturn(Optional.of(testUser1)); // 이 호출은 발생하지 않음

            assertThatThrownBy(() -> eventService.appliyToEvent(testEvent1.getId(), testUser1.getUserId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("User already applied or is participating in this event.");

            verify(eventRepositoryMock).findById(testEvent1.getId());
            verify(userRepositoryMock, never()).findById(testUser1.getUserId()); // alreadyExists 조건에서 걸러짐
        }

        @Test
        @DisplayName("이벤트 정원이 다 찼으면 (채팅방 참여자 수 기준) IllegalStateException을 던진다 ('이벤트가 다 찼습니다.')")
        void whenEventIsFullBasedOnChatroomParticipants_throwsIllegalStateException() {
            // Given: testEvent1의 maxParticipants를 1로 설정 (매니저(eventCreator)는 이벤트 참여자일 뿐, 채팅방 참여자는 아님)
            testEvent1.setMaxParticipants(1);

            User existingChatUser = User.builder().userId(50L).name("기존채팅참여자").build();
            testChatroomForEvent1.getParticipants().add(ChatroomParticipant.builder().user(existingChatUser).chatroom(testChatroomForEvent1).build());

            // eventRepositoryMock.findById는 appliyToEvent 시작 시 호출되므로 이 스터빙은 필요합니다.
            // setUpAppliy @BeforeEach에서 이미 when(eventRepositoryMock.findById(testEvent1.getId())).thenReturn(Optional.of(testEvent1));
            // 설정되어 있으므로, 여기서는 중복 설정할 필요가 없습니다.
            // 만약 setUpAppliy에 없다면 여기서 설정해야 합니다.

            // When & Then: testUser1이 신청 시도
            assertThatThrownBy(() -> eventService.appliyToEvent(testEvent1.getId(), testUser1.getUserId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이벤트가 다 찼습니다.");

            // Verify:
            // 1. 이벤트 조회는 발생했어야 함
            verify(eventRepositoryMock).findById(testEvent1.getId());
            // 2. 정원 초과로 인해 사용자 조회 로직은 실행되지 않았어야 함
            verify(userRepositoryMock, never()).findById(testUser1.getUserId());
        }

    }
}
