package com.runinto.user.service;

import com.runinto.event.domain.EventParticipant;
import com.runinto.event.dto.response.EventResponse;
import com.runinto.exception.user.*;
import com.runinto.user.domain.Role;
import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserH2Repository;
import com.runinto.user.dto.request.RegisterRequest;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class UserService {

    private final UserH2Repository userH2Repository;

    public UserService(final UserH2Repository userH2Repository) {
        this.userH2Repository = userH2Repository;
    }

    public User findById(final Long userId) {
        return userH2Repository.findById(userId)
                .orElseThrow(() -> new UserIdNotFoundException("User id not found: " + userId + " ."));
    }

    public void saveUser(final User user) {
        userH2Repository.save(user);
    }

    public List<EventResponse> getJoinedEvents(Long userId) {
        if (!userH2Repository.existsByUserId(userId)) {
            throw new UserIdNotFoundException("User id not found: " + userId + " .");
        }
        List<EventParticipant> participats = userH2Repository.findParticipationsByUserId(userId);
        return EventResponse.from(participats);
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

    public User registerUser(RegisterRequest request,String imgUrl) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .imgUrl(imgUrl)
                .description(request.getDescription())
                .gender(request.getGender())
                .age(request.getAge())
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .build();

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

    public User authenticate(String email, String password) {
        User user = userH2Repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("해당 이메일의 유저가 존재하지 않습니다: " + email));

        if (!user.getPassword().equals(password)) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    public void ensureUserNameAndEmailAreUnique(String name, String email) {
        if (existsByName(name)) {
            throw new UserNameAlreadyExistsException("이미 존재하는 이름입니다: " + name);
        }
        if (existsByEmail(email)) {
            throw new UserEmailAlreadyExistsException("이미 존재하는 이메일입니다: " + email);
        }
    }
}
