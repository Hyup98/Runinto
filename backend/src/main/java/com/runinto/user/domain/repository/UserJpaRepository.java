package com.runinto.user.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.user.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {
    @Query("""
        SELECT e.event 
        FROM EventParticipant e 
        WHERE e.user.userId = :userId
    """)
    List<Event> findJoinedEvents(@Param("userId") Long userId);

    Optional<User> findByEmail(String email);

    boolean existsByName(String name);

    boolean existsByEmail(String email);

    boolean existsByUserId(Long userId);

    /*@EntityGraph(attributePaths = {
            "eventParticipants.event",
            "chatParticipations.chatroom"
    })
    @Query("SELECT u FROM User u WHERE u.userId = :id")
    Optional<User> findWithAssociationsById(@Param("id") Long id);*/

    @EntityGraph(attributePaths = {
            "eventParticipants.event",
            "chatParticipations.chatroom"
    })
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findWithAssociationsByEmail(@Param("email") String email);
}
