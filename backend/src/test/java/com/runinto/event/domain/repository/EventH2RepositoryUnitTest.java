package com.runinto.event.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Time;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventH2Repository 단위 테스트 (Mockito 기반)")
class EventH2RepositoryUnitTest {

    @Mock
    private EventJpaRepository eventJpaRepositoryMock; // EventJpaRepository를 모킹

    @InjectMocks
    private EventRepository eventH2Repository; // 테스트 대상 클래스

    private Event sampleEvent;

    @BeforeEach
    void setUp() {
        sampleEvent = Event.builder()
                .id(1L)
                .title("샘플 이벤트")
                .description("이것은 샘플 이벤트입니다.")
                .maxParticipants(10)
                .creationTime(Time.valueOf(LocalTime.now()))
                .latitude(37.5665)
                .longitude(126.9780)
                .categories(new HashSet<>())
                .build();
        // 필요하다면 EventCategory도 추가
        EventCategory sampleCategory = EventCategory.builder().category(EventType.ACTIVITY).event(sampleEvent).build();
        sampleEvent.getEventCategories().add(sampleCategory);
    }

    @Nested
    @DisplayName("findById 메소드는")
    class Describe_findById {
        @Test
        @DisplayName("존재하는 ID가 주어지면 EventJpaRepository.findById를 호출하고 결과를 반환한다")
        void whenIdExists_callsJpaFindByIdAndReturnsResult() {
            // Given
            when(eventJpaRepositoryMock.findById(sampleEvent.getId())).thenReturn(Optional.of(sampleEvent));

            // When
            Optional<Event> result = eventH2Repository.findById(sampleEvent.getId());

            // Then
            assertThat(result).isPresent().contains(sampleEvent);
            verify(eventJpaRepositoryMock).findById(sampleEvent.getId());
        }

        @Test
        @DisplayName("존재하지 않는 ID가 주어지면 EventJpaRepository.findById를 호출하고 빈 Optional을 반환한다")
        void whenIdNotExists_callsJpaFindByIdAndReturnsEmpty() {
            // Given
            long nonExistingId = 99L;
            when(eventJpaRepositoryMock.findById(nonExistingId)).thenReturn(Optional.empty());

            // When
            Optional<Event> result = eventH2Repository.findById(nonExistingId);

            // Then
            assertThat(result).isNotPresent();
            verify(eventJpaRepositoryMock).findById(nonExistingId);
        }
    }

    @Nested
    @DisplayName("save 메소드는")
    class Describe_save {
        @Test
        @DisplayName("Event 객체가 주어지면 EventJpaRepository.save를 호출하고 저장된 객체를 반환한다")
        void whenEventGiven_callsJpaSaveAndReturnsSavedEvent() {
            // Given
            Event newEvent = Event.builder().title("새 이벤트").build();
            when(eventJpaRepositoryMock.save(newEvent)).thenReturn(newEvent); // save는 보통 인자로 받은 객체나 ID가 채워진 객체를 반환

            // When
            Event savedEvent = eventH2Repository.save(newEvent);

            // Then
            assertThat(savedEvent).isEqualTo(newEvent);
            verify(eventJpaRepositoryMock).save(newEvent);
        }
    }

    @Nested
    @DisplayName("findAll 메소드는")
    class Describe_findAll {
        @Test
        @DisplayName("EventJpaRepository.findAll을 호출하고 모든 이벤트 목록을 반환한다")
        void callsJpaFindAllAndReturnsListOfEvents() {
            // Given
            List<Event> expectedEvents = List.of(sampleEvent);
            when(eventJpaRepositoryMock.findAll()).thenReturn(expectedEvents);

            // When
            List<Event> actualEvents = eventH2Repository.findAll();

            // Then
            assertThat(actualEvents).isEqualTo(expectedEvents);
            verify(eventJpaRepositoryMock).findAll();
        }
    }

    @Nested
    @DisplayName("findByCategory 메소드는")
    class Describe_findByCategory {
        @Test
        @DisplayName("카테고리 Set이 주어지면 EventJpaRepository.findByCategories를 호출하고 결과를 반환한다")
        void whenCategoriesGiven_callsJpaFindByCategoriesAndReturnsResult() {
            // Given
            Set<EventType> categories = Set.of(EventType.ACTIVITY);
            List<Event> expectedEvents = List.of(sampleEvent);
            when(eventJpaRepositoryMock.findByCategories(categories)).thenReturn(expectedEvents);

            // When
            List<Event> actualEvents = eventH2Repository.findByCategory(categories);

            // Then
            assertThat(actualEvents).isEqualTo(expectedEvents);
            verify(eventJpaRepositoryMock).findByCategories(categories);
        }
    }

    @Nested
    @DisplayName("findByArea 메소드는")
    class Describe_findByArea {
        @Test
        @DisplayName("좌표 범위가 주어지면 EventJpaRepository.findByArea를 호출하고 결과를 반환한다")
        void whenCoordinatesGiven_callsJpaFindByAreaAndReturnsResult() {
            // Given
            double neLat = 38.0, neLng = 128.0, swLat = 37.0, swLng = 126.0;
            List<Event> expectedEvents = List.of(sampleEvent);
            when(eventJpaRepositoryMock.findByArea(neLat, neLng, swLat, swLng)).thenReturn(expectedEvents);

            // When
            List<Event> actualEvents = eventH2Repository.findByArea(neLat, neLng, swLat, swLng);

            // Then
            assertThat(actualEvents).isEqualTo(expectedEvents);
            verify(eventJpaRepositoryMock).findByArea(neLat, neLng, swLat, swLng);
        }
    }

    @Nested
    @DisplayName("getSize 메소드는")
    class Describe_getSize {
        @Test
        @DisplayName("EventJpaRepository.count를 호출하고 이벤트 개수를 반환한다")
        void callsJpaCountAndReturnsSize() {
            // Given
            long expectedCount = 10L;
            when(eventJpaRepositoryMock.count()).thenReturn(expectedCount);

            // When
            int size = eventH2Repository.getSize();

            // Then
            assertThat(size).isEqualTo((int) expectedCount);
            verify(eventJpaRepositoryMock).count();
        }
    }

    @Nested
    @DisplayName("delete 메소드는")
    class Describe_delete {
        @Test
        @DisplayName("삭제할 이벤트가 존재하면 EventJpaRepository.deleteById를 호출하고 true를 반환한다")
        void whenEventExists_callsJpaDeleteByIdAndReturnsTrue() {
            // Given
            when(eventJpaRepositoryMock.existsById(sampleEvent.getId())).thenReturn(true);
            doNothing().when(eventJpaRepositoryMock).deleteById(sampleEvent.getId()); // void 메소드 모킹

            // When
            boolean result = eventH2Repository.delete(sampleEvent);

            // Then
            assertThat(result).isTrue();
            verify(eventJpaRepositoryMock).existsById(sampleEvent.getId());
            verify(eventJpaRepositoryMock).deleteById(sampleEvent.getId());
        }

        @Test
        @DisplayName("삭제할 이벤트가 존재하지 않으면 EventJpaRepository.deleteById를 호출하지 않고 false를 반환한다")
        void whenEventNotExists_doesNotCallJpaDeleteByIdAndReturnsFalse() {
            // Given
            Event nonExistingEvent = Event.builder().id(99L).build();
            when(eventJpaRepositoryMock.existsById(nonExistingEvent.getId())).thenReturn(false);

            // When
            boolean result = eventH2Repository.delete(nonExistingEvent);

            // Then
            assertThat(result).isFalse();
            verify(eventJpaRepositoryMock).existsById(nonExistingEvent.getId());
            verify(eventJpaRepositoryMock, never()).deleteById(anyLong()); // deleteById 호출 안됨 검증
        }
    }

    @Nested
    @DisplayName("clear 메소드는")
    class Describe_clear {
        @Test
        @DisplayName("EventJpaRepository.deleteAll을 호출한다")
        void callsJpaDeleteAll() {
            // Given
            doNothing().when(eventJpaRepositoryMock).deleteAll(); // void 메소드 모킹

            // When
            eventH2Repository.clear();

            // Then
            verify(eventJpaRepositoryMock).deleteAll();
        }
    }
}