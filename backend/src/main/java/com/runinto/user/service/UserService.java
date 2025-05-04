package com.runinto.user.service;

import com.runinto.event.domain.Event;
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

    public void saveUser(final User user) {
        userH2Repository.save(user);
    }

    public List<EventResponse> getJoinedEvents(Long userId) {
        List<Event> joinedEvents = userH2Repository.findJoinedEvents(userId);
        return joinedEvents.stream()
                .map(EventResponse::from)
                .toList();
    }

}
