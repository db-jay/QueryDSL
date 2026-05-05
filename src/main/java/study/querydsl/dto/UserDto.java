package study.querydsl.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {
    private String name;
    private int age;

    // 학습: Projections.constructor는 DTO의 생성자 파라미터 순서와 타입이 select 절과 맞아야 안전하다.
    public UserDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
