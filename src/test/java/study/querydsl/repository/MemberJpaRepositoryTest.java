package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    // * JPA
    @Test
    public void basicTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member foundMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(foundMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

//    * QueryDsl
@Test
public void basicQuerydslTest() {
    Member member = new Member("member1", 10);
    memberJpaRepository.save(member);

    Member findMember = memberJpaRepository.findById(member.getId()).get();
    assertThat(findMember).isEqualTo(member);

    // 학습: 같은 요구사항(findAll, findByUsername)을 순수 JPA와 Querydsl로 각각 검증해두면 API는 같고 구현 방식만 다르다는 점이 잘 보인다.
    List<Member> result1 = memberJpaRepository.findAllQuerydsl();
    assertThat(result1).containsExactly(member);

    List<Member> result2 =
            memberJpaRepository.findByUsernameQuerydsl("member1");
    assertThat(result2).containsExactly(member);
}
}
