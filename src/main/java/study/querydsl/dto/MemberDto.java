package study.querydsl.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // 기본 생성자
// 학습: Projections.bean/fields는 기본 생성자로 DTO를 만든 뒤 setter 또는 필드에 값을 채운다.
public class MemberDto {
    private String username;
    private int age;
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
