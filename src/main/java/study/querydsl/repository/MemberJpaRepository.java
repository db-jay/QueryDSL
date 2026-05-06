package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;


import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.*;

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
}
