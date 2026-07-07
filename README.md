# 박람회(행사) 예약 관리 플랫폼 (expo-platform)

전체 관리자·박람회 관리자·고객 3개 역할로 운영하는 박람회 예약 관리 플랫폼.
결제(카카오페이/PortOne)·QR 전자 티켓·운영자 교육(LMS)을 포함한다.

## 기술 스택
Java 17 · Spring Boot 3.3.x · Gradle · Spring Data JPA · H2(파일 모드) · Thymeleaf + jQuery · Spring Security

## 실행
```bash
./gradlew bootRun
# http://localhost:8080  (관리자 로그인: admin / admin123)
# H2 콘솔: http://localhost:8080/h2-console  (JDBC URL: jdbc:h2:file:./data/expodb)
```

## 결제 연동(선택)
환경변수 3개를 설정하면 실제 카카오페이 결제가 활성화되고, 없으면 Mock 결제로 폴백한다.
```
PORTONE_STORE_ID, PORTONE_CHANNEL_KEY, PORTONE_API_SECRET
```

## 문서
- [개발기획서.md](개발기획서.md) · [기능명세서.md](기능명세서.md) · [CLAUDE.md](CLAUDE.md)

## 진행 상황
- [x] M1 기반(프로젝트·보안·관리자 로그인·시드)
- [x] 디자인 시스템(전시장 웨이파인딩)
- [x] M2 박람회·예약(Expo CRUD+코드발급 · 고객 로그인 · 행사목록/상세 · 예약)
- [x] M3 결제·QR·체크인(카카오페이/PortOne+Mock · 입장 배지 QR · 코드 체크인)
- [x] M4 관리자(대시보드 실집계 · 예약자 명단/검색/CSV/취소 · VIP 배너)
- [ ] M5 LMS(교육 생성·배정·YouTube 수강율·이수)

## 시연 계정
- 전체 관리자 `admin / admin123` · 박람회 관리자 `event1 / event123` · 고객 `01012345678 / 1234`
