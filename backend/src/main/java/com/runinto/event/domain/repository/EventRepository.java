package com.runinto.event.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventType;
import com.runinto.event.dto.request.FindEventRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@Primary
public class EventRepository {

    private final EventJpaRepository eventJpaRepository;

    public EventRepository(EventJpaRepository eventJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
    }

    public Optional<Event> findById(long id) {
        return eventJpaRepository.findById(id);
    }

    public Event save(Event event) {
        return eventJpaRepository.save(event);
    }

    public List<Event> findAll() {
        return eventJpaRepository.findAll();
    }

    public List<Event> findByCategory(Set<EventType> categories) {
        return eventJpaRepository.findByCategories(categories);
    }

    public List<Event> findByHostUserId(Long userId) {
        // 👇 JpaRepository에 정의한 메소드를 호출하도록 수정합니다.
        return eventJpaRepository.findByHostUserId(userId);
    }

    public List<Event> findByArea(double nelatitude, double nelongitude, double swlatitude, double swlongitude) {
        return eventJpaRepository.findByArea(nelatitude, nelongitude, swlatitude, swlongitude);
    }

    public List<Event> findByDynamicCondition(FindEventRequest request) {
        // Specification.where(null)은 항상 참인 조건으로 시작하여, 다른 조건들을 and로 연결합니다.
        Specification<Event> spec = Specification.where(null);

        // 위치 정보 필터링 조건 추가
        if (request.getSwlatitude() != null && request.getNelatitude() != null &&
                request.getSwlongitude() != null && request.getNelongitude() != null) {
            spec = spec.and(EventSpecifications.isInArea(
                    request.getSwlatitude(),
                    request.getNelatitude(),
                    request.getSwlongitude(),
                    request.getNelongitude()
            ));
        }

        // 카테고리 필터링 조건 추가
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            spec = spec.and(EventSpecifications.hasCategoryIn(request.getCategories()));
        }

        // 'isPublic' 필터링 (FindEventRequest에 isPublic 필드가 있고, 이를 필터링 조건에 넣고 싶다면)
        // if (request.getIsPublic() != null) {
        //     spec = spec.and(EventSpecifications.isPublic(request.getIsPublic()));
        // }


        return eventJpaRepository.findAll(spec); // JpaSpecificationExecutor의 findAll 메서드 사용
    }

    public int getSize(){
        return (int) eventJpaRepository.count();
    }

    public boolean delete(Event event) {
        if (eventJpaRepository.existsById(event.getId())) {
            eventJpaRepository.deleteById(event.getId());
            return true;
        }
        return false;
    }

    public void clear() {
        eventJpaRepository.deleteAll();
    }

    public List<Event> findByGridIdIn(List<String> gridIds) {
        return eventJpaRepository.findByGridIdIn(gridIds);
    }

    public List<Event> findWithCategoriesById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return eventJpaRepository.findWithCategoriesByIdIn(ids);
    }

    public List<Event> findWithCategoriesByIdIn(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return eventJpaRepository.findWithCategoriesByIdIn(ids);
    }

    public Page<Long> findIdsWithPaging(Specification<Event> spec, Pageable pageable) {
        Page<Event> eventPage = eventJpaRepository.findAll(spec, pageable);
        // Page<Event>를 Page<Long> (ID 페이징 결과)으로 변환하여 반환
        return eventPage.map(Event::getId);
    }
    public List<Event> findByGridIdInWithCategories(List<String> gridIds) {
        return eventJpaRepository.findByGridIdInWithCategories(gridIds);
    }


}
