package com.runinto.user.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventParticipant;
import com.runinto.user.domain.Gender;
import com.runinto.user.domain.Role;
import com.runinto.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException; // 예외 테스트용 임포트

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy; // 예외 테스트용 임포트
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserH2Repository 단위 테스트 (Updated)")
class UserH2RepositoryTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private UserH2Repository userH2Repository;

    private User sampleUser;
    private User anotherUser;
    private Event sampleEvent1;
    private Event sampleEvent2;
    // private Chatroom sampleChatroom; // Chatroom 관련 테스트가 있다면 사용

    @BeforeEach
    void setUp() {
        sampleEvent1 = Event.builder().id(1L).title("테스트 이벤트 1").build();
        sampleEvent2 = Event.builder().id(2L).title("테스트 이벤트 2").build();

        EventParticipant participant1ForSampleUser = EventParticipant.builder().event(sampleEvent1).build();

        sampleUser = User.builder()
                .userId(1L)
                .name("테스트유저")
                .email("testuser@example.com")
                .password("password")
                .imgUrl("default.png")
                .description("테스트 유저입니다.")
                .gender(Gender.MALE)
                .age(25)
                .role(Role.USER)
                .eventParticipants(new HashSet<>()) // NPE 방지를 위해 초기화
                .chatParticipations(new HashSet<>()) // NPE 방지를 위해 초기화
                .build();

        participant1ForSampleUser.setUser(sampleUser);
        sampleUser.getEventParticipants().add(participant1ForSampleUser);


        anotherUser = User.builder()
                .userId(2L)
                .name("다른유저")
                .email("anotheruser@example.com")
                .password("password123")
                .imgUrl("another.png")
                .description("다른 유저입니다.")
                .gender(Gender.FEMALE)
                .age(30)
                .role(Role.USER)
                .eventParticipants(new HashSet<>()) // NPE 방지를 위해 초기화
                .chatParticipations(new HashSet<>()) // NPE 방지를 위해 초기화
                .build();
    }

    @Nested
    @DisplayName("findById 메소드는")
    class Describe_findById {
        @Nested
        @DisplayName("존재하는 사용자 ID가 주어지면")
        class Context_with_existing_user_id {
            @Test
            @DisplayName("해당 사용자를 포함하는 Optional을 반환한다")
            void it_returns_optional_with_user() {
                when(userJpaRepository.findById(sampleUser.getUserId())).thenReturn(Optional.of(sampleUser));

                Optional<User> foundUser = userH2Repository.findById(sampleUser.getUserId());

                assertThat(foundUser).isPresent();
                assertThat(foundUser.get()).isEqualTo(sampleUser);
                verify(userJpaRepository).findById(sampleUser.getUserId());
            }
        }

        @Nested
        @DisplayName("존재하지 않는 사용자 ID가 주어지면")
        class Context_with_non_existing_user_id {
            @Test
            @DisplayName("빈 Optional을 반환한다")
            void it_returns_empty_optional() {
                when(userJpaRepository.findById(999L)).thenReturn(Optional.empty());

                Optional<User> foundUser = userH2Repository.findById(999L);

                assertThat(foundUser).isNotPresent();
                verify(userJpaRepository).findById(999L);
            }
        }

        @Nested
        @DisplayName("ID로 null이 주어지면")
        class Context_with_null_id {
            @Test
            @DisplayName("IllegalArgumentException을 던진다")
            void it_throws_illegal_argument_exception() {
                when(userJpaRepository.findById(null)).thenThrow(new IllegalArgumentException("ID must not be null!"));

                assertThatThrownBy(() -> userH2Repository.findById(null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("ID must not be null!");

                verify(userJpaRepository).findById(null);
            }
        }
    }

    @Nested
    @DisplayName("save 메소드는")
    class Describe_save {
        @Test
        @DisplayName("새로운 사용자 객체를 저장하면 저장된 사용자(ID 포함)를 반환한다")
        void it_saves_new_user_and_returns_with_id() {
            User newUser = User.builder().name("새유저")
                    .email("new@example.com")
                    .password("newpass")
                    .imgUrl("new.png")
                    .description("새 유저")
                    .gender(Gender.MALE)
                    .age(20)
                    .role(Role.USER).build();

            User savedUser = User.builder()
                    .userId(3L).name("새유저")
                    .email("new@example.com")
                    .password("newpass")
                    .imgUrl("new.png")
                    .description("새 유저")
                    .gender(Gender.MALE)
                    .age(20)
                    .role(Role.USER).build();

            when(userJpaRepository.save(any(User.class))).thenReturn(savedUser);

            User result = userH2Repository.save(newUser);

            assertThat(result).isEqualTo(savedUser);
            assertThat(result.getUserId()).isNotNull();
            verify(userJpaRepository).save(newUser);
        }

        @Test
        @DisplayName("기존 사용자 객체를 업데이트하면 업데이트된 사용자를 반환한다")
        void it_updates_existing_user_and_returns_updated() {
            sampleUser.setDescription("업데이트");
            when(userJpaRepository.save(any(User.class))).thenReturn(sampleUser);

            User updatedUser = userH2Repository.save(sampleUser);

            assertThat(updatedUser.getDescription()).isEqualTo("업데이트");
            verify(userJpaRepository).save(sampleUser);
        }

        @Test
        @DisplayName("사용자 저장 시 참여 이벤트 정보(EventParticipant)도 함께 저장된다 (Cascade)")
        void it_saves_user_with_event_participants() {
            User userWithEvents = User.builder()
                    .name("이벤트참여유저")
                    .email("eventuser@example.com")
                    .build();
            EventParticipant newParticipant = EventParticipant.builder().event(sampleEvent2).user(userWithEvents).build();
            userWithEvents.getEventParticipants().add(newParticipant);

            User savedUserWithEvents = User.builder()
                    .userId(4L)
                    .name("이벤트참여유저")
                    .email("eventuser@example.com")
                    .eventParticipants(Set.of(newParticipant)) // 저장 후에는 ID 등이 채워질 수 있음
                    .build();

            when(userJpaRepository.save(userWithEvents)).thenReturn(savedUserWithEvents);

            User result = userH2Repository.save(userWithEvents);

            assertThat(result).isEqualTo(savedUserWithEvents);
            verify(userJpaRepository).save(userWithEvents);
        }
        
        @Nested
        @DisplayName("필수 필드가 null인 사용자를 저장하려고 할 때")
        class Context_with_null_required_field {
            @Test
            @DisplayName("DataIntegrityViolationException을 던진다")
            void it_throws_data_integrity_violation_exception() {
                User invalidUser = User.builder().name(null).email("invalid@example.com").build();
                when(userJpaRepository.save(invalidUser)).thenThrow(new DataIntegrityViolationException("필수 필드 누락"));

                assertThatThrownBy(() -> userH2Repository.save(invalidUser))
                        .isInstanceOf(DataIntegrityViolationException.class)
                        .hasMessageContaining("필수 필드 누락");
                
                verify(userJpaRepository).save(invalidUser);
            }
        }
    }

    @Nested
    @DisplayName("delete 메소드는")
    class Describe_delete {
        @Test
        @DisplayName("사용자 ID로 삭제를 시도하면 UserJpaRepository.deleteById를 호출한다")
        void it_calls_jpa_deleteById() {
            Long userIdToDelete = sampleUser.getUserId();
            // deleteById는 void를 반환하므로 doNothing().when() 사용
            doNothing().when(userJpaRepository).deleteById(userIdToDelete);

            userH2Repository.delete(userIdToDelete);

            verify(userJpaRepository).deleteById(userIdToDelete);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 삭제를 시도해도 예외가 발생하지 않는다")
        void it_does_not_throw_exception_for_non_existing_id() {
            Long nonExistingUserId = 999L;
            doNothing().when(userJpaRepository).deleteById(nonExistingUserId);

            userH2Repository.delete(nonExistingUserId);

            verify(userJpaRepository).deleteById(nonExistingUserId);
        }
    }

    @Nested
    @DisplayName("findByEmail 메소드는 (findWithAssociationsByEmail 사용)")
    class Describe_findByEmail {
        @Nested
        @DisplayName("존재하는 이메일이 주어지면")
        class Context_with_existing_email {
            @Test
            @DisplayName("연관 관계(이벤트 참여)가 로드된 사용자를 포함하는 Optional을 반환한다")
            void it_returns_optional_with_user_and_associations() {
                when(userJpaRepository.findWithAssociationsByEmail(sampleUser.getEmail()))
                        .thenReturn(Optional.of(sampleUser));

                Optional<User> foundUserOpt = userH2Repository.findByEmail(sampleUser.getEmail());

                assertThat(foundUserOpt).isPresent();
                User foundUser = foundUserOpt.get();
                assertThat(foundUser.getEmail()).isEqualTo(sampleUser.getEmail());
                assertThat(foundUser.getEventParticipants()).isNotNull();
                assertThat(foundUser.getEventParticipants()).hasSize(1); // setUp에서 추가한 participant 확인
                assertThat(foundUser.getEventParticipants().iterator().next().getEvent()).isEqualTo(sampleEvent1);

                verify(userJpaRepository).findWithAssociationsByEmail(sampleUser.getEmail());
            }

            @Test
            @DisplayName("사용자가 여러 이벤트에 참여했을 때 모든 참여 정보가 로드된다")
            void it_loads_all_event_participations() {
                EventParticipant participant2 = EventParticipant.builder().event(sampleEvent2).user(sampleUser).build();
                sampleUser.getEventParticipants().add(participant2); // 두 번째 이벤트 참여 추가

                when(userJpaRepository.findWithAssociationsByEmail(sampleUser.getEmail()))
                        .thenReturn(Optional.of(sampleUser));
                
                Optional<User> foundUserOpt = userH2Repository.findByEmail(sampleUser.getEmail());

                assertThat(foundUserOpt).isPresent();
                User foundUser = foundUserOpt.get();
                assertThat(foundUser.getEventParticipants()).hasSize(2);
                assertThat(foundUser.getEventParticipants()).extracting(EventParticipant::getEvent)
                                                            .containsExactlyInAnyOrder(sampleEvent1, sampleEvent2);
                
                verify(userJpaRepository).findWithAssociationsByEmail(sampleUser.getEmail());
            }
        }

        @Nested
        @DisplayName("존재하지 않는 이메일이 주어지면")
        class Context_with_non_existing_email {
            @Test
            @DisplayName("빈 Optional을 반환한다")
            void it_returns_empty_optional() {
                when(userJpaRepository.findWithAssociationsByEmail("nonexistent@example.com"))
                        .thenReturn(Optional.empty());

                Optional<User> foundUser = userH2Repository.findByEmail("nonexistent@example.com");

                assertThat(foundUser).isNotPresent();
                verify(userJpaRepository).findWithAssociationsByEmail("nonexistent@example.com");
            }
        }
    }

    @Nested
    @DisplayName("findJoinedEvents 메소드는")
    class Describe_findJoinedEvents {
        @Nested
        @DisplayName("사용자가 참여한 이벤트가 있을 때")
        class Context_when_user_has_joined_events {
            @Test
            @DisplayName("참여한 이벤트 목록을 반환한다")
            void it_returns_list_of_joined_events() {
                // sampleUser는 setUp에서 sampleEvent1에 참여하도록 설정됨
                when(userJpaRepository.findJoinedEvents(sampleUser.getUserId()))
                        .thenReturn(List.of(sampleEvent1));

                List<Event> joinedEvents = userH2Repository.findJoinedEvents(sampleUser.getUserId());

                assertThat(joinedEvents).isNotNull();
                assertThat(joinedEvents).hasSize(1);
                assertThat(joinedEvents.get(0)).isEqualTo(sampleEvent1);
                verify(userJpaRepository).findJoinedEvents(sampleUser.getUserId());
            }
        }

        @Nested
        @DisplayName("사용자가 참여한 이벤트가 없을 때")
        class Context_when_user_has_no_joined_events {
            @Test
            @DisplayName("빈 목록을 반환한다")
            void it_returns_empty_list() {
                when(userJpaRepository.findJoinedEvents(anotherUser.getUserId()))
                        .thenReturn(Collections.emptyList());

                List<Event> joinedEvents = userH2Repository.findJoinedEvents(anotherUser.getUserId());

                assertThat(joinedEvents).isNotNull();
                assertThat(joinedEvents).isEmpty();
                verify(userJpaRepository).findJoinedEvents(anotherUser.getUserId());
            }
        }
         @Nested
        @DisplayName("존재하지 않는 사용자 ID로 조회할 때")
        class Context_with_non_existing_user_id {
            @Test
            @DisplayName("빈 목록을 반환한다 (또는 예외 처리에 따라 다름)")
            void it_returns_empty_list_or_handles_exception() {
                // UserService에서는 existsByUserId 체크 후 UserIdNotFoundException을 던지지만,
                // Repository 레벨에서는 보통 빈 목록을 반환하거나, 호출하는 쪽에서 ID 유효성을 검사합니다.
                // 현재 UserH2Repository는 UserJpaRepository를 직접 호출하므로, JpaRepository의 동작을 따릅니다.
                // UserJpaRepository.findJoinedEvents가 ID가 없을 때 빈 리스트를 반환한다고 가정합니다.
                Long nonExistingUserId = 999L;
                when(userJpaRepository.findJoinedEvents(nonExistingUserId)).thenReturn(Collections.emptyList());

                List<Event> joinedEvents = userH2Repository.findJoinedEvents(nonExistingUserId);

                assertThat(joinedEvents).isNotNull();
                assertThat(joinedEvents).isEmpty();
                verify(userJpaRepository).findJoinedEvents(nonExistingUserId);
            }
        }
    }

    @Nested
    @DisplayName("deleteAll 메소드는")
    class Describe_deleteAll {
        @Test
        @DisplayName("UserJpaRepository.deleteAll을 호출한다")
        void it_calls_jpa_deleteAll() {
            doNothing().when(userJpaRepository).deleteAll();

            userH2Repository.deleteAll();

            verify(userJpaRepository).deleteAll();
        }
    }

    @Nested
    @DisplayName("existsByName 메소드는")
    class Describe_existsByName {
        @Test
        @DisplayName("해당 이름의 사용자가 존재하면 true를 반환한다")
        void when_name_exists_returns_true() {
            when(userJpaRepository.existsByName(sampleUser.getName())).thenReturn(true);

            boolean exists = userH2Repository.existsByName(sampleUser.getName());

            assertThat(exists).isTrue();
            verify(userJpaRepository).existsByName(sampleUser.getName());
        }

        @Test
        @DisplayName("해당 이름의 사용자가 없으면 false를 반환한다")
        void when_name_not_exists_returns_false() {
            when(userJpaRepository.existsByName("없는이름")).thenReturn(false);

            boolean exists = userH2Repository.existsByName("없는이름");

            assertThat(exists).isFalse();
            verify(userJpaRepository).existsByName("없는이름");
        }
    }

    @Nested
    @DisplayName("existsByEmail 메소드는")
    class Describe_existsByEmail {
        @Test
        @DisplayName("해당 이메일의 사용자가 존재하면 true를 반환한다")
        void when_email_exists_returns_true() {
            when(userJpaRepository.existsByEmail(sampleUser.getEmail())).thenReturn(true);

            boolean exists = userH2Repository.existsByEmail(sampleUser.getEmail());

            assertThat(exists).isTrue();
            verify(userJpaRepository).existsByEmail(sampleUser.getEmail());
        }

        @Test
        @DisplayName("해당 이메일의 사용자가 없으면 false를 반환한다")
        void when_email_not_exists_returns_false() {
            when(userJpaRepository.existsByEmail("없는이메일@example.com")).thenReturn(false);

            boolean exists = userH2Repository.existsByEmail("없는이메일@example.com");

            assertThat(exists).isFalse();
            verify(userJpaRepository).existsByEmail("없는이메일@example.com");
        }
    }

    @Nested
    @DisplayName("existsByUserId 메소드는")
    class Describe_existsByUserId {
        @Test
        @DisplayName("해당 ID의 사용자가 존재하면 true를 반환한다")
        void when_id_exists_returns_true() {
            when(userJpaRepository.existsByUserId(sampleUser.getUserId())).thenReturn(true);

            boolean exists = userH2Repository.existsByUserId(sampleUser.getUserId());

            assertThat(exists).isTrue();
            verify(userJpaRepository).existsByUserId(sampleUser.getUserId());
        }

        @Test
        @DisplayName("해당 ID의 사용자가 없으면 false를 반환한다")
        void when_id_not_exists_returns_false() {
            when(userJpaRepository.existsByUserId(999L)).thenReturn(false);


            boolean exists = userH2Repository.existsByUserId(999L);

            assertThat(exists).isFalse();
            verify(userJpaRepository).existsByUserId(999L);
        }
    }
}