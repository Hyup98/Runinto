package com.runinto.event.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface EventJpaRepository extends JpaRepository<Event, Long> , JpaSpecificationExecutor<Event> {

    @Query("SELECT e FROM Event e JOIN e.eventCategories ec WHERE ec.category IN :categories")
    List<Event> findByCategories(@Param("categories") Set<EventType> categories); // 파라미터 타입을 Set<EventType>로 명확히

    @Query("SELECT e FROM Event e WHERE e.latitude >= :swlat AND e.latitude <= :nelat AND e.longitude >= :swlng AND e.longitude <= :nelng")
    List<Event> findByArea(
            @Param("nelat") double nelat,
            @Param("nelng") double nelng,
            @Param("swlat") double swlat,
            @Param("swlng") double swlng);


    @Query("SELECT DISTINCT e FROM Event e JOIN FETCH e.eventCategories ec WHERE e.gridId IN :gridIds") // 수정 후
    List<Event> findByGridIdInWithCategories(@Param("gridIds") List<String> gridIds);

    @Query("SELECT DISTINCT e FROM Event e JOIN FETCH e.eventCategories WHERE e.id IN :ids")
    List<Event> findWithCategoriesByIdIn(@Param("ids") List<Long> ids);


    List<Event> findByGridIdIn(List<String> gridIds);
    List<Event> findByHostUserId(Long userId);

}