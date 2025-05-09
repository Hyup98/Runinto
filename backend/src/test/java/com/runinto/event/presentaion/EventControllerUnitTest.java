package com.runinto.event.presentaion;

import com.runinto.event.domain.Event;
import com.runinto.event.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
public class EventControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Test
    void testGetAllEventsWithQueryParams() throws Exception {
        List<Event> dummyEvents = List.of(
                Event.builder().id(1L).title("이벤트 1").build()
        );

        when(eventService.findByDynamicCondition(any())).thenReturn(dummyEvents);

        mockMvc.perform(get("/events")
                        .param("swLat", "37.50")
                        .param("neLat", "37.60")
                        .param("swLng", "127.00")
                        .param("neLng", "127.10")
                        .param("category", "ACTIVITY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events[0].title").value("이벤트 1"));
    }
}
