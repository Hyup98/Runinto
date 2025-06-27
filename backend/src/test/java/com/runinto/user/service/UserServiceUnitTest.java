package com.runinto.user.service;

import com.runinto.event.domain.Event; // Assuming Event domain class exists for EventResponse
import com.runinto.event.dto.response.EventResponse;
import com.runinto.exception.user.*;
import com.runinto.user.domain.Gender;
import com.runinto.user.domain.Role;
import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserH2Repository; // Using the type mocked in the original test
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Time;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserH2Repository userH2RepositoryMock; // Mocking the type used in original test and constructor

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .name("테스트유저")
                .email("test@example.com")
                .password("password123") // In real scenarios, this would be encoded
                .imgUrl("/img/test.png")
                .description("테스트 설명")
                .gender(Gender.MALE)
                .age(30)
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("findById - 성공: 사용자 ID로 사용자 조회")
    void findById_shouldReturnUser_whenUserExists() {
        // given
        when(userH2RepositoryMock.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

        // when
        User foundUser = userService.findById(testUser.getUserId());

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUserId()).isEqualTo(testUser.getUserId());
        assertThat(foundUser.getName()).isEqualTo(testUser.getName());
        verify(userH2RepositoryMock).findById(testUser.getUserId());
    }

    @Test
    @DisplayName("findById - 실패: 존재하지 않는 사용자 ID로 조회 시 UserIdNotFoundException 발생")
    void findById_shouldThrowUserIdNotFoundException_whenUserDoesNotExist() {
        // given
        Long nonExistentUserId = 99L;
        when(userH2RepositoryMock.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.findById(nonExistentUserId))
                .isInstanceOf(UserIdNotFoundException.class)
                .hasMessageContaining("User id not found: " + nonExistentUserId);
        verify(userH2RepositoryMock).findById(nonExistentUserId);
    }

    @Test
    @DisplayName("saveUser - 성공: 사용자 정보 저장")
    void saveUser_shouldCallRepositorySave() {
        // given - testUser from setUp

        // when
        userService.saveUser(testUser);

        // then
        verify(userH2RepositoryMock).save(testUser);
    }

    @Test
    @DisplayName("getJoinedEvents - 성공: 참여한 이벤트 목록 반환")
    void getJoinedEvents_shouldReturnListOfEventResponses_whenUserExistsAndHasEvents() {
        // given
        Long userId = testUser.getUserId();
        Event event1 = Event.builder().id(101L).title("이벤트 1").description("설명1").maxParticipants(10).latitude(35.0).longitude(129.0).creationTime(Time.valueOf(LocalTime.now())).build();
        Event event2 = Event.builder().id(102L).title("이벤트 2").description("설명2").maxParticipants(10).latitude(35.0).longitude(129.0).creationTime(Time.valueOf(LocalTime.now())).build();
        List<Event> joinedEventsFromRepo = List.of(event1, event2);
        when(userH2RepositoryMock.existsByUserId(userId)).thenReturn(true); //
        when(userH2RepositoryMock.findJoinedEvents(userId)).thenReturn(joinedEventsFromRepo); //

        // when
        List<EventResponse> eventResponses = userService.getJoinedEvents(userId);

        // then
        assertThat(eventResponses).isNotNull();
        assertThat(eventResponses).hasSize(2);
        assertThat(eventResponses.get(0).getTitle()).isEqualTo("이벤트 1");
        assertThat(eventResponses.get(1).getTitle()).isEqualTo("이벤트 2");
        verify(userH2RepositoryMock).existsByUserId(userId);
        verify(userH2RepositoryMock).findJoinedEvents(userId);
    }

    @Test
    @DisplayName("getJoinedEvents - 성공: 참여한 이벤트 없음")
    void getJoinedEvents_shouldReturnEmptyList_whenUserExistsAndHasNoEvents() {
        // given
        Long userId = testUser.getUserId();
        when(userH2RepositoryMock.existsByUserId(userId)).thenReturn(true); //
        when(userH2RepositoryMock.findJoinedEvents(userId)).thenReturn(Collections.emptyList()); //

        // when
        List<EventResponse> eventResponses = userService.getJoinedEvents(userId);

        // then
        assertThat(eventResponses).isNotNull();
        assertThat(eventResponses).isEmpty();
        verify(userH2RepositoryMock).existsByUserId(userId);
        verify(userH2RepositoryMock).findJoinedEvents(userId);
    }

    @Test
    @DisplayName("getJoinedEvents - 실패: 존재하지 않는 사용자로 조회 시 UserIdNotFoundException 발생")
    void getJoinedEvents_shouldThrowUserIdNotFoundException_whenUserDoesNotExist() {
        // given
        Long nonExistentUserId = 99L;
        when(userH2RepositoryMock.existsByUserId(nonExistentUserId)).thenReturn(false); //

        // when / then
        assertThatThrownBy(() -> userService.getJoinedEvents(nonExistentUserId))
                .isInstanceOf(UserIdNotFoundException.class)
                .hasMessageContaining("User id not found: " + nonExistentUserId); //
        verify(userH2RepositoryMock).existsByUserId(nonExistentUserId);
        verify(userH2RepositoryMock, never()).findJoinedEvents(anyLong());
    }

    @Test
    @DisplayName("registerUser - 성공: 새로운 사용자 등록")
    void registerUser_shouldReturnRegisteredUser_whenNameAndEmailAreUnique() {
        // given
        when(userH2RepositoryMock.existsByName(testUser.getName())).thenReturn(false); //
        when(userH2RepositoryMock.existsByEmail(testUser.getEmail())).thenReturn(false); //
        when(userH2RepositoryMock.save(any(User.class))).thenReturn(testUser);

        // when
        User registeredUser = userService.registerUser(testUser);

        // then
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getName()).isEqualTo(testUser.getName());
        verify(userH2RepositoryMock).existsByName(testUser.getName());
        verify(userH2RepositoryMock).existsByEmail(testUser.getEmail());
        verify(userH2RepositoryMock).save(testUser);
    }

    @Test
    @DisplayName("registerUser - 실패: 이미 존재하는 이름으로 등록 시 UserNameAlreadyExistsException 발생")
    void registerUser_shouldThrowUserNameAlreadyExistsException_whenNameExists() {
        // given
        when(userH2RepositoryMock.existsByName(testUser.getName())).thenReturn(true); //

        // when / then
        assertThatThrownBy(() -> userService.registerUser(testUser))
                .isInstanceOf(UserNameAlreadyExistsException.class) //
                .hasMessageContaining("User with name '" + testUser.getName() + "' already exists.");
        verify(userH2RepositoryMock).existsByName(testUser.getName());
        verify(userH2RepositoryMock, never()).existsByEmail(anyString());
        verify(userH2RepositoryMock, never()).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser - 실패: 이미 존재하는 이메일로 등록 시 UserEmailAlreadyExistsException 발생")
    void registerUser_shouldThrowUserEmailAlreadyExistsException_whenEmailExists() {
        // given
        when(userH2RepositoryMock.existsByName(testUser.getName())).thenReturn(false); //
        when(userH2RepositoryMock.existsByEmail(testUser.getEmail())).thenReturn(true); //

        // when / then
        assertThatThrownBy(() -> userService.registerUser(testUser))
                .isInstanceOf(UserEmailAlreadyExistsException.class) //
                .hasMessageContaining("User with email '" + testUser.getEmail() + "' already exists.");
        verify(userH2RepositoryMock).existsByName(testUser.getName());
        verify(userH2RepositoryMock).existsByEmail(testUser.getEmail());
        verify(userH2RepositoryMock, never()).save(any(User.class));
    }

    @Test
    @DisplayName("existsByName - 이름 존재 시 true 반환")
    void existsByName_shouldReturnTrue_whenNameExists() {
        // given
        String name = "existingName";
        when(userH2RepositoryMock.existsByName(name)).thenReturn(true);

        // when
        boolean result = userService.existsByName(name);

        // then
        assertThat(result).isTrue();
        verify(userH2RepositoryMock).existsByName(name);
    }

    @Test
    @DisplayName("existsByName - 이름 존재하지 않을 시 false 반환")
    void existsByName_shouldReturnFalse_whenNameDoesNotExist() {
        // given
        String name = "newName";
        when(userH2RepositoryMock.existsByName(name)).thenReturn(false);

        // when
        boolean result = userService.existsByName(name);

        // then
        assertThat(result).isFalse();
        verify(userH2RepositoryMock).existsByName(name);
    }

    @Test
    @DisplayName("existsByEmail - 이메일 존재 시 true 반환")
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        // given
        String email = "existing@example.com";
        when(userH2RepositoryMock.existsByEmail(email)).thenReturn(true);

        // when
        boolean result = userService.existsByEmail(email);

        // then
        assertThat(result).isTrue();
        verify(userH2RepositoryMock).existsByEmail(email);
    }

    @Test
    @DisplayName("existsByEmail - 이메일 존재하지 않을 시 false 반환")
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
        // given
        String email = "new@example.com";
        when(userH2RepositoryMock.existsByEmail(email)).thenReturn(false);

        // when
        boolean result = userService.existsByEmail(email);

        // then
        assertThat(result).isFalse();
        verify(userH2RepositoryMock).existsByEmail(email);
    }

    @Test
    @DisplayName("authenticate - 성공: 올바른 이메일과 비밀번호로 인증")
    void authenticate_shouldReturnUser_whenCredentialsAreCorrect() {
        // given
        String email = testUser.getEmail();
        String password = testUser.getPassword();
        when(userH2RepositoryMock.findByEmail(email)).thenReturn(Optional.of(testUser));

        // when
        User authenticatedUser = userService.authenticate(email, password);

        // then
        assertThat(authenticatedUser).isNotNull();
        assertThat(authenticatedUser.getEmail()).isEqualTo(email);
        verify(userH2RepositoryMock).findByEmail(email);
    }

    @Test
    @DisplayName("authenticate - 실패: 존재하지 않는 이메일로 인증 시 UserNotFoundException 발생")
    void authenticate_shouldThrowUserNotFoundException_whenEmailDoesNotExist() {
        // given
        String nonExistentEmail = "unknown@example.com";
        String password = "password123";
        when(userH2RepositoryMock.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.authenticate(nonExistentEmail, password))
                .isInstanceOf(UserNotFoundException.class) //
                .hasMessageContaining("해당 이메일의 유저가 존재하지 않습니다: " + nonExistentEmail);
        verify(userH2RepositoryMock).findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("authenticate - 실패: 잘못된 비밀번호로 인증 시 InvalidPasswordException 발생")
    void authenticate_shouldThrowInvalidPasswordException_whenPasswordIsIncorrect() {
        // given
        String email = testUser.getEmail();
        String wrongPassword = "wrongPassword";
        when(userH2RepositoryMock.findByEmail(email)).thenReturn(Optional.of(testUser));

        // when / then
        assertThatThrownBy(() -> userService.authenticate(email, wrongPassword))
                .isInstanceOf(InvalidPasswordException.class) //
                .hasMessageContaining("비밀번호가 일치하지 않습니다.");
        verify(userH2RepositoryMock).findByEmail(email);
    }

    @Test
    @DisplayName("ensureUserNameAndEmailAreUnique - 성공: 이름과 이메일 모두 고유할 때 예외 없음")
    void ensureUserNameAndEmailAreUnique_shouldNotThrowException_whenNameAndEmailAreUnique() {
        // given
        String name = "uniqueName";
        String email = "unique@example.com";
        when(userH2RepositoryMock.existsByName(name)).thenReturn(false);
        when(userH2RepositoryMock.existsByEmail(email)).thenReturn(false);

        // when / then
        assertDoesNotThrow(() -> userService.ensureUserNameAndEmailAreUnique(name, email));
        verify(userH2RepositoryMock).existsByName(name);
        verify(userH2RepositoryMock).existsByEmail(email);
    }

    @Test
    @DisplayName("ensureUserNameAndEmailAreUnique - 실패: 이름이 이미 존재할 때 UserNameAlreadyExistsException 발생")
    void ensureUserNameAndEmailAreUnique_shouldThrowUserNameAlreadyExistsException_whenNameExists() {
        // given
        String existingName = "existingName";
        String email = "unique@example.com";
        when(userH2RepositoryMock.existsByName(existingName)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> userService.ensureUserNameAndEmailAreUnique(existingName, email))
                .isInstanceOf(UserNameAlreadyExistsException.class) //
                .hasMessageContaining("이미 존재하는 이름입니다: " + existingName);
        verify(userH2RepositoryMock).existsByName(existingName);
        verify(userH2RepositoryMock, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("ensureUserNameAndEmailAreUnique - 실패: 이메일이 이미 존재할 때 UserEmailAlreadyExistsException 발생")
    void ensureUserNameAndEmailAreUnique_shouldThrowUserEmailAlreadyExistsException_whenEmailExists() {
        // given
        String name = "uniqueName";
        String existingEmail = "existing@example.com";
        when(userH2RepositoryMock.existsByName(name)).thenReturn(false);
        when(userH2RepositoryMock.existsByEmail(existingEmail)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> userService.ensureUserNameAndEmailAreUnique(name, existingEmail))
                .isInstanceOf(UserEmailAlreadyExistsException.class) //
                .hasMessageContaining("이미 존재하는 이메일입니다: " + existingEmail);
        verify(userH2RepositoryMock).existsByName(name);
        verify(userH2RepositoryMock).existsByEmail(existingEmail);
    }
}