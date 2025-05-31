package com.runinto.event.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.Set;
public class EventSpecifications {

    public static Specification<Event> isInArea(Double swLat, Double neLat, Double swLng, Double neLng) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction(); // 항상 true로 시작하는 Predicate

            // 위도, 경도 조건은 Event 엔티티의 필드명을 사용합니다.
            if (swLat != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("latitude"), swLat));
            }
            if (neLat != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("latitude"), neLat));
            }
            if (swLng != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("longitude"), swLng));
            }
            if (neLng != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("longitude"), neLng));
            }
            return predicate;
        };
    }

    public static Specification<Event> hasCategoryIn(Set<EventType> categories) {
        return (root, query, criteriaBuilder) -> {
            // Event 엔티티는 eventCategories 필드를 통해 EventCategory와 관계를 맺고,
            // EventCategory는 category 필드 (EventType)를 가집니다.
            query.distinct(true); // 중복된 Event 방지
            Join<Event, EventCategory> eventCategoryJoin = root.join("eventCategories");
            return eventCategoryJoin.get("category").in(categories);
        };
    }

    public static Specification<Event> isPublic(Boolean isPublicFlag) {
        return (root, query, criteriaBuilder) -> {
            if (isPublicFlag == null) {
                // isPublic 파라미터가 없으면 공개 이벤트(true)만 조회하도록 기본값 설정
                return criteriaBuilder.equal(root.get("isPublic"), true);
            }
            return criteriaBuilder.equal(root.get("isPublic"), isPublicFlag);
        };
    }

}