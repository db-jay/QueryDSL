package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;


import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@RequiredArgsConstructor
@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    // 학습: 순수 JPA 메서드는 EntityManager로 JPQL 문자열을 직접 실행하고, Querydsl 메서드는 같은 리포지토리 안에서 queryFactory로 타입 안전 쿼리를 만든다.
    private final JPAQueryFactory queryFactory;

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(em.find(Member.class, id));
    }


//    # JPA
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

//    # QueryDSL
    public List<Member> findAllQuerydsl() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsernameQuerydsl(String username) {
        return queryFactory
                // 학습: Querydsl 버전은 문자열 "m.username" 대신 member.username 경로를 써서 리팩터링 시 컴파일 오류로 바로 확인할 수 있다.
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

//    # Builder를 사용한 동적 쿼리 조회
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {

        // 학습: 검색 조건 DTO를 하나로 받으면 컨트롤러/테스트/리포지토리가 같은 형태의 조건 객체를 공유할 수 있다.
        BooleanBuilder builder = new BooleanBuilder();
        if(hasText(condition.getUsername())) {
           builder.and(member.username.eq(condition.getUsername()));
        }

        if(hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }

        if(condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }

        if(condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder) // builder로 동적쿼리 조회
                .fetch();
    }

//    # Where절을 사용한 동적 쿼리 조회

    public List<MemberTeamDto> search(MemberSearchCondition condition) {

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                // 학습: where(varargs)는 null 을 자동으로 무시하므로, 조건 메서드를 잘게 나누면 BooleanBuilder 없이도 동적 쿼리를 조립할 수 있다.
                .where(
                        userNameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    private BooleanExpression userNameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }


    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe): null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe): null;
    }
}
