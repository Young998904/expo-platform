# 박람회(행사) 예약 관리 플랫폼 (expo-platform) — 프로젝트 규칙

> 심화 프로젝트 중 하나(독립 저장소). 상세 설계: [개발기획서.md](개발기획서.md), [기능명세서.md](기능명세서.md)

## 주제
전체 관리자가 여러 박람회를 개설하고, 박람회 관리자가 자기 행사를 운영하며,
고객이 모바일웹에서 예약·결제(카카오페이)·QR 입장하는 예약 관리 플랫폼.
운영자(박람회 관리자) 교육을 위한 **LMS(교육 이수)** 기능을 내장한다.

## 역할 (3종)
- **전체 관리자(SUPER_ADMIN)**: 박람회 CRUD·계정 발급·배너·통계·교육 관리.
- **박람회 관리자(EVENT_ADMIN)**: 담당 박람회 운영(예약/체크인/콘텐츠), 배정 교육 수강.
- **고객(USER)**: 행사 탐색·예약·결제·QR·나의 예약. 전화번호+PIN 경량 세션.

## 도메인 핵심 규칙
- **선결제**: 결제 성공 시에만 예약이 CONFIRMED 되고 좌석을 점유한다(PENDING은 좌석 미점유).
- **예약 상태**: PENDING → CONFIRMED → CHECKED_IN / CONFIRMED → CANCEL_REQUESTED → CANCELLED.
- **좌석 잔여** = capacity − Σheadcount(CONFIRMED·CHECKED_IN). 확정 트랜잭션에서 재검증(오버셀 방지).
- **매출/좌석 집계는 Reservation 기준**(Payment는 저장소 없는 하위 엔티티).
- **LMS 이수** = 시청 최대 위치 ≥ 영상 길이의 **95%**. 폴링 5초, 최대 위치 증가 시에만 저장.

## 결제(PortOne) 보안
- 자격정보는 **환경변수로만**: `PORTONE_STORE_ID`, `PORTONE_CHANNEL_KEY`, `PORTONE_API_SECRET`.
  하드코딩·채팅 노출 금지. 3키가 모두 있을 때만 실연동, 없으면 Mock 폴백.
- 결제 검증은 **서버 사이드**(status=PAID·금액 일치)로 위변조를 막는다.

---

# 공통 개발 규칙 (모든 프로젝트 공통)

## 기술 스택 (확정 · 변경 금지)
- **Java 17 / Spring Boot 3.x / Gradle** (Boot 4·Java 25로 올리지 않는다)
- Spring Data JPA (**MyBatis 미사용**), **H2 파일 모드**(추후 MySQL 전환은 application.yml 교체)
- Thymeleaf + 일부 jQuery/Ajax, Spring Security(세션·역할 기반)

## 코딩 컨벤션 (필수)
- **모든 주석은 한글**로 '왜'를 설명. 공개 Service/Controller에 한글 Javadoc.
- **화면 텍스트는 기본 한글**(관용 영어 허용: 로그인·QR·대시보드 등), **코드 식별자는 영어**.
- 계층 분리(controller/service/repository/dto), 엔티티 직접 노출 금지(DTO).
- JPA N+1 회피(fetch join·`@EntityGraph`), 트랜잭션 경계는 service, 비밀번호·PIN은 BCrypt.

## 스킬 참고 주의
- `code-reviewer`·`spring-data-jpa` 스킬은 Boot 4/Java 25 기준이라 **일반 가이드로만 참고**하고,
  최신 전용 문법 제안은 Boot 3.x/Java 17에 맞게 걸러 적용한다.

## 작업 규칙
- 코드 변경 시 `main` 브랜치에 커밋·push (브랜치 분리하지 않음).

## 시연 계정
- 전체 관리자: `admin` / `admin123` (DataSeeder 자동 생성)
