package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable); // 단순 페이지 쿼리
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable); // 카운트랑 페이징 분리 쿼리
}
