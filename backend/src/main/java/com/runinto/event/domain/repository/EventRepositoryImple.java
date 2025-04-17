package com.runinto.event.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;

import java.util.*;

public interface EventRepositoryImple {
    public Optional<Event> findById(long id);
    public void save(Event event);
    public List<Event> findAll();
    public List<Event> findByCategory(List<EventCategory> categories);
    public List<Event> findByArea(double nelatitude, double nelongitude, double swlatitude, double swlongitude);
    public void delete(long id);
}
