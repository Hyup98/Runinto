package com.runinto.user.service;

import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserRepositoryImple;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepositoryImple userMemoryRepository;

    public Optional<User> getUser(final Long userId) {
        return userMemoryRepository.findById(userId);
    }

    public void saveUser(final User user) {
        userMemoryRepository.save(user);
    }

}
