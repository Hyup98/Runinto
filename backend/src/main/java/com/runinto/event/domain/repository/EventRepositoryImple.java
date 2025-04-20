package com.runinto.event.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;

import java.util.*;

public interface EventRepositoryImple {
    public Optional<Event> findById(long id);
    public void save(Event event);
    public List<Event> findAll();
    public List<Event> findByCategory(Set<EventType> categorys);
    public List<Event> findByArea(double nelatitude, double nelongitude, double swlatitude, double swlongitude);
    public boolean delete(long id);
    public void clear();
}
