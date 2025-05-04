package com.runinto.user.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface  UserRepositoryImple {
    public Optional<User> findById(Long id);
    public void save(User user);
    public void delete(Long id);
    List<Event> findJoinedEvents(Long userId);
}
