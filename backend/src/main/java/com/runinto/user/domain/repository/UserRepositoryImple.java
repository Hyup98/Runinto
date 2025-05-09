package com.runinto.user.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface  UserRepositoryImple {
    public Optional<User> findById(Long id);
    public Optional<User> findByEmail(String email);
    public User save(User user);
    public void delete(Long id);
    List<Event> findJoinedEvents(Long userId);
    public boolean existsByName(String name);
    public boolean existsByEmail(String email);
}
