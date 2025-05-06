package com.runinto.event.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

/*
class EventMemoryRepositoryTest {

    EventMemoryRepository eventMemoryRepository = new EventMemoryRepository();

    @BeforeEach
    void setUp() {
        for (long i = 1; i <= 10; i++) {
            Event event = Event.builder()
                    .eventId(i)
                    .title("이벤트 " + i)
                    .description("설명입니다 " + i)
                    .maxParticipants(10)
                    .creationTime(Time.valueOf(LocalTime.now()))
                    .latitude(37.56 + (i * 0.001)) // 위치를 약간씩 다르게
                    .longitude(127.01 + (i * 0.001))
                    .chatroomId(i)
                    .participants((int) (i % 5))
                    .categories(Set.of(new EventCategory(i,EventType.ACTIVITY, i)))
                    .build();

            eventMemoryRepository.save(event);
        }
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findById() {
        //given & when
        Event event = eventMemoryRepository.findById(1L).orElse(null);
        Event event1 = eventMemoryRepository.findById(5L).orElse(null);
        Event event2 = eventMemoryRepository.findById(15L).orElse(null);

        //then
        assertEquals(event.getEventId(),1L);
        assertEquals(event1.getEventId(),5L);
        assertNull(event2);
    }

    @Test
    void save() {
        //given
        Event event = Event.builder()
                .eventId(11L)
                .title("이벤트 " + 11L)
                .description("설명입니다 " + 11L)
                .maxParticipants(10)
                .creationTime(Time.valueOf(LocalTime.now()))
                .latitude(37.56 + (11 * 0.001)) // 위치를 약간씩 다르게
                .longitude(127.01 + (11 * 0.001))
                .chatroomId(11L)
                .participants((int) (11 % 5))
                .categories(Set.of(new EventCategory(11,EventType.ACTIVITY, 1L)))
                .build();

        //when
        eventMemoryRepository.save(event);

        //then
        assertThat(event).usingRecursiveComparison().isEqualTo(eventMemoryRepository.findById(11L).orElse(null));
        assertEquals(11, eventMemoryRepository.getSize());
    }

    @Test
    void findAll() {
        //given && when
        List<Event> events = eventMemoryRepository.findAll();
        //then
        assertEquals(10, events.size());

    }

    @Test
    void findByCategory() {
        //given
        Set<EventType> categories = Set.of(EventType.ACTIVITY);
        Set<EventType> categories1 = Set.of(EventType.EAT);

        //when
        List<Event> events = eventMemoryRepository.findByCategory(categories);
        List<Event> events1 = eventMemoryRepository.findByCategory(categories1);

        //then
        assertEquals(10, events.size());
        assertEquals(0, events1.size());
    }

    @Test
    void findByArea() {

        double swLat = 37.562;
        double swLng = 127.012;
        double neLat = 37.566;
        double neLng = 127.016;

        // when
        List<Event> result = eventMemoryRepository.findByArea(neLat, neLng, swLat, swLng);

        // then
        assertEquals(5, result.size());

        for (Event event : result) {
            assertTrue(event.getLatitude() >= swLat && event.getLatitude() <= neLat);
            assertTrue(event.getLongitude() >= swLng && event.getLongitude() <= neLng);
        }
    }
}*/
