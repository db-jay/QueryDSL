package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @PersistenceContext
    EntityManager em;

    // 학습: 테스트마다 같은 EntityManager를 쓰므로 QueryFactory를 필드로 두고 재사용하면 쿼리 본문 읽기가 쉬워진다.
    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        // 학습: Querydsl의 시작점은 EntityManager로 JPAQueryFactory를 만드는 것이다.
        queryFactory = new JPAQueryFactory(em);

        // 학습: 테스트 데이터를 미리 고정해두면 where, orderBy, groupBy 결과를 예측하면서 문법을 연습하기 쉽다.
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }


    @Test
    public void startJPQL() {
        // 학습: 먼저 문자열 JPQL로 같은 조회를 작성해두면, 아래 Querydsl 코드가 무엇을 대체하는지 비교하기 쉽다.
        //member1을 찾아라.
        String qlString =
                "select m from Member m " +
                        "where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
//        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        // 학습: QMember("m")의 "m"은 JPQL의 별칭(alias)과 같은 역할이라서 from member m 과 대응된다.
//        QMember m = new QMember("m");

        Member findMember = queryFactory
                // 학습: selectFrom(member)는 select(member).from(member)를 줄여 쓴 기본 패턴이다.
                .selectFrom(member) // static import
                .from(member)
                // 학습: Member.username 같은 문자열 필드가 QMember에서는 타입 안전한 경로가 되어 eq(), lt() 같은 조건 메서드를 바로 붙일 수 있다.
                // 학습: BooleanExpression은 and()로 조합할 수 있어서 복잡한 조건도 메서드 체인으로 자연스럽게 확장된다.
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        // 학습: where(varargs)는 조건을 쉼표로 나열해도 and 로 묶여서 읽기 쉽다.
                        member.username.eq("member1"), // and 조건 ','로도 표시 가능
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() {
        // 학습: fetch는 리스트, fetchOne은 단건, fetchFirst는 limit 1 성격이라 반환 형태에 따라 메서드를 고른다.
//        List<Member> fetch = queryFactory.selectFrom(member)
//                .fetch();
//
//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();
//
//        queryFactory
//                .selectFrom(member)
////                .limit(1).fetchOne() = .fetchFirst()
//                .fetchFirst();


//        QueryResults<Member> results = queryFactory
//                .selectFrom(member)
//                .fetchResults();
//
//        results.getTotal(); // 카운트 쿼리 +1
//        List<Member> content = results.getResults();

        // 학습: fetchCount/fetchResults는 편하지만 최신 Querydsl/JPA 조합에서는 deprecated라서 동작과 한계를 함께 익혀두는 편이 좋다.
        // 카운트쿼리
        queryFactory
                .selectFrom(member)
                .fetchCount();
    }


    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                // 학습: orderBy에는 우선순위대로 여러 정렬 조건을 넣고, null 처리 규칙도 함께 줄 수 있다.
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                // 학습: offset/limit은 SQL의 페이징 구문으로 내려가며, 보통 정렬(orderBy)과 함께 써야 결과가 안정적이다.
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        // 학습: fetchResults는 내용 조회와 전체 카운트를 같이 가져와 편하지만 실제로는 쿼리가 2번 나간다.
        QueryResults<Member> memberQueryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

//        카운트쿼리 1 + 컨텐츠 조회 쿼리 1 총 2번 쿼리
        assertThat(memberQueryResults.getTotal()).isEqualTo(4);
        assertThat(memberQueryResults.getResults().size()).isEqualTo(2);
        assertThat(memberQueryResults.getLimit()).isEqualTo(2);
        assertThat(memberQueryResults.getOffset()).isEqualTo(1);
    }


    /*
    select
        count(member1),
        sum(member1.age),
        avg(member1.age),
        max(member1.age),
        min(member1.age)
    from
        Member member1
     */
    /*
    select
    count(m1_0.member_id),
    sum(m1_0.age),
    avg(cast(m1_0.age as float(53))),
    max(m1_0.age),
    min(m1_0.age)
    from
    member m1_0
    */
    @Test
    public void aggregation() {
        List<Tuple> result = queryFactory // queryDsl이 제공하는 tuple
                .select(
                        // 학습: 집계 결과는 엔티티 한 건이 아니라 여러 컬럼 묶음이라 Tuple로 받는 것이 기본이다.
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                ).from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4L);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                // 학습: 연관관계 조인은 join(member.team, team)처럼 경로와 조인 대상을 함께 써서 표현한다.
                .join(member.team, team)
                // 학습: groupBy 뒤의 select에는 그룹 기준 컬럼과 집계 컬럼을 같이 두는 패턴이 가장 자주 나온다.
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }
    
    
    // 팀 A에 소속된 모든 회원
    @Test
    public void join() {
        List<Member> result = queryFactory
                .selectFrom(member)
                // 학습: 연관관계 조인은 member.team 같은 객체 경로를 따라가므로, SQL의 ON 절 FK 조건을 직접 쓰지 않아도 된다.
                .join(member.team, team) // join(조인 대상, 별칭으로 사용할 Q타입)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    // 세타 조인
    @Test
    public void theta_join() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                // 학습: from(member, team)은 두 테이블을 독립적으로 올린 뒤 where에서 조건을 묶는 세타 조인 패턴이다.
                .from(member, team)  // from 절에 여러 엔티티를 선택해서 세타 조인
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    // 조인 ON 절
    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and
     t.name='teamA'
     */
    @Test
    public void join_on() throws Exception {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                // 학습: leftJoin(...).on(...)은 조인 대상을 줄이는 조건을 ON 절로 보내서, 왼쪽 엔티티(member)는 유지하고 오른쪽(team)만 거를 수 있다.
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

    }

    // 연관관계 없는 엔티티 외부 조인
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                // 학습: leftJoin(team).on(...)처럼 경로 없이 조인하면 연관관계가 없어도 이름 같은 임의 조건으로 외부 조인을 만들 수 있다.
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    // 페치조인이 없는 경우
    @Test void fetchJoinNo() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        // 학습: fetch join이 없으면 member만 먼저 조회되고 team은 프록시로 남아서, 실제 접근 전까지 로딩되지 않는다.
        boolean loaded =  emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치조인 미적용").isFalse();
        System.out.println("loaded = " + loaded);
    }
    
    // 페치조인이 있는 경우
    @Test void fetchJoin() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                // 학습: fetchJoin()은 조인한 연관 엔티티를 한 번에 같이 조회해서 N+1 확인용 테스트에서 자주 쓴다.
                .join(member.team, team).fetchJoin() // 페치 조인
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded =  emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치조인 미적용").isTrue();
        System.out.println("loaded = " + loaded);

    }
}
