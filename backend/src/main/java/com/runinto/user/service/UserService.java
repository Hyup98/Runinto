package com.runinto.user.service;

import com.runinto.event.domain.Event;
import com.runinto.exception.UserEmailAlreadyExistsException;
import com.runinto.exception.UserNameAlreadyExistsException;
import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserH2Repository;
import com.runinto.user.domain.repository.UserRepositoryImple;
import com.runinto.user.dto.response.EventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepositoryImple userH2Repository;

    public UserService(final UserH2Repository userH2Repository) {
        this.userH2Repository = userH2Repository;
    }

    public Optional<User> getUser(final Long userId) {
        return userH2Repository.findById(userId);
    }

    public Optional<User> findByEmail(final String email) {
        return userH2Repository.findByEmail(email);
    }

    public void saveUser(final User user) {
        userH2Repository.save(user);
    }

    public List<EventResponse> getJoinedEvents(Long userId) {
        List<Event> joinedEvents = userH2Repository.findJoinedEvents(userId);
        return joinedEvents.stream()
                .map(EventResponse::from)
                .toList();
    }

    public User registerUser(final User user) {
        if(userH2Repository.existsByName(user.getName())) {
            throw new UserNameAlreadyExistsException("User with name '" + user.getName() + "' already exists.");
        }
        if(userH2Repository.existsByEmail(user.getEmail())) {
            throw new UserEmailAlreadyExistsException("User with email '" + user.getEmail() + "' already exists.");
        }
        return userH2Repository.save(user);
    }

    public boolean existsByName(String name) {
        return userH2Repository.existsByName(name);
    }

    public boolean existsByEmail(String email) {
        return userH2Repository.existsByEmail(email);
    }
}
