package com.runinto.user.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.user.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.runinto.event.domain.EventParticipant;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {

    @Query("""
        SELECT ep
        FROM EventParticipant ep
        WHERE ep.user.userId = :userId
    """)
    List<EventParticipant> findParticipationsByUserId(@Param("userId") Long userId);


    Optional<User> findByEmail(String email);

    boolean existsByName(String name);

    boolean existsByEmail(String email);

    boolean existsByUserId(Long userId);

    @EntityGraph(attributePaths = {
            "eventParticipants.event",
            "chatParticipations.chatroom"
    })
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findWithAssociationsByEmail(@Param("email") String email);
}
