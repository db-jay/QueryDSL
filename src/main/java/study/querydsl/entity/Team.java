package study.querydsl.entity;
import lombok.*;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {
    @Id @GeneratedValue
    @Column(name = "team_id")
    private Long id;
    private String name;
    // 학습: 컬렉션 연관관계는 QTeam.members 같은 ListPath 로 생성되어 컬렉션 조인/조건 실습에 활용된다.
    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
    public Team(String name) {
        this.name = name;
    }
}
