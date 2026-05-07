package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        // 학습: 요청 파라미터(username, teamName, ageGoe, ageLoe)를 MemberSearchCondition에 바인딩한 뒤 그대로 리포지토리 동적 쿼리에 넘긴다.
        return memberJpaRepository.search(condition);
    }
}
