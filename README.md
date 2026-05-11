# Querydsl 학습 프로젝트

Spring Boot + JPA + Querydsl을 따라가며 기본 문법부터
동적 쿼리, DTO 조회, 순수 JPA 리포지토리, Spring Data JPA + Querydsl 페이징까지
학습한 프로젝트입니다.

## 학습 목표
Querydsl의 기본 문법 → DTO 조회 → 동적 쿼리 → 리포지토리 적용 → 페이징 최적화 흐름을 숙지한다.

## 학습 내용


- JPQL vs Querydsl 비교
- Q-Type 기반 기본 조회 문법
- 검색 조건, 정렬, 페이징
- 조인 / on절 / 세타 조인 / 페치 조인
- DTO 조회
  - JPQL `new`
  - `Projections.bean`
  - `Projections.fields`
  - `Projections.constructor`
  - `@QueryProjection`
- 동적 쿼리
  - `BooleanBuilder`
  - `where` 다중 파라미터 방식
- 순수 JPA + Querydsl 리포지토리
- Spring Data JPA + Querydsl 사용자 정의 리포지토리
- Querydsl 페이징
  - 단순 페이징
  - count 쿼리 분리 최적화

## 프로젝트 구조

- `QuerydslBasicTest`
  - Querydsl 기본 문법 학습용 테스트
- `MemberJpaRepository`
  - 순수 JPA + Querydsl 비교
- `MemberRepository`, `MemberRepositoryImpl`
  - Spring Data JPA + Querydsl 적용
- `MemberController`
  - 조회 API 실습
- `InitMember`
  - 로컬 실행 시 샘플 데이터 초기화

## 조회 API

- /v1/members : 순수 JPA + Querydsl 조회
- /v2/members : Spring Data JPA + Querydsl 단순 페이징
- /v3/members : count 쿼리 최적화 페이징

