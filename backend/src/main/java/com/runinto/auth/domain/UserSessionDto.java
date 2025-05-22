package com.runinto.auth.domain; // DTO를 위한 적절한 패키지를 만드세요.

import com.runinto.user.domain.Role; // User 클래스에서 사용하는 Role enum
import java.io.Serializable;

public class UserSessionDto implements Serializable {

    private static final long serialVersionUID = 1L; // 직렬화 버전 UID

    private Long userId;
    private String name;
    private String email;
    private Role role; // 역할 정보는 권한 체크 등에 유용합니다.
    private String imgUrl; // 프로필 이미지 URL 등 UI 표시에 필요할 수 있습니다.

    // 기본 생성자
    public UserSessionDto() {
    }

    // 모든 필드를 받는 생성자 (User 엔티티로부터 DTO를 만들 때 편리)
    public UserSessionDto(Long userId, String name, String email, Role role, String imgUrl) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.imgUrl = imgUrl;
    }

    // User 엔티티를 UserSessionDto로 변환하는 정적 팩토리 메소드 (선택 사항이지만 편리함)
    public static UserSessionDto fromUser(com.runinto.user.domain.User user) {
        if (user == null) {
            return null;
        }
        return new UserSessionDto(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getImgUrl()
        );
    }

    // Getter 메소드들
    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    // Setter는 필요에 따라 추가 (세션에 저장된 후 변경할 일이 없다면 불필요)
    // toString(), equals(), hashCode() 등도 필요에 따라 오버라이드
}