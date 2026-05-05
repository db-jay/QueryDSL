package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // 기본 생성자
// 학습: Projections.bean/fields는 기본 생성자로 DTO를 만든 뒤 setter 또는 필드에 값을 채운다.
public class MemberDto {
    private String username;
    private int age;

    // 학습: @QueryProjection을 붙이면 컴파일 시점에 이 생성자를 호출하는 QMemberDto가 생성되어 타입 안전한 DTO 조회가 가능해진다.
    @QueryProjection // QType 바로 생성
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
