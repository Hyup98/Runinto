package com.runinto.event.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;

import java.util.*;

public interface EventRepositoryImple {
    Optional<Event> findById(long id);
    Event save(Event event);
    List<Event> findAll();
    List<Event> findByCategory(Set<EventType> categorys);
    List<Event> findByArea(double nelatitude, double nelongitude, double swlatitude, double swlongitude);
    boolean delete(Event event);
    void clear();
}
