package com.runinto.chat.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class Chatroom {

    public static Long chatRoomCount = 0L;

    private Long id;

    //추후에 이 리스트가 다른 서버로 요청 보내고 받는 기능을 하는 그런 친구로 수정할 예정
    private List<Long> applicants;

    public void addApplicant(Long userId) {
        if (applicants == null) {
            applicants = new ArrayList<>();
        }
        applicants.add(userId);
    }

    public void removeApplicant(Long userId) {
        if (applicants != null) {
            applicants.remove(userId);
        }
    }

    public Chatroom(Long id) {
        this.id = id;
        this.applicants = new ArrayList<>();
    }
}
