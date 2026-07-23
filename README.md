# 📖 다이어리 프로젝트 Backend

Spring Boot와 React를 사용한 개인 다이어리 웹 애플리케이션

## 기술 스택

### Backend
- Java 17
- Spring Boot 3.2.1
- AWS RDS (PostgreSQL 18.1)
- Spring Security + JWT
- Spring Data JPA

### Server
- AWS EC2 Ubuntu

## 주요 기능

- [X] 사용자 회원가입/로그인 (JWT 인증)
  - [ ] JWT 인증 시 각 예외별로 구분 (만료/위조/형식 오류)
- [X] 다이어리 작성
  - [X] 이미지 삽입
  - [X] 제목 / 날짜 / 태그 / 내용
- [ ] 다이어리 수정
  - 기존 게시글 내용 불러오기
- [ ] 다이어리 삭제
- [ ] 다이어리 목록 조회
- [ ] 다이어리 검색 기능
- [ ] 태그 기능
  - [ ] 태그별 게시글 조회
- [ ] 날짜별 필터링

## CI/CD
- [X] Jenkins를 이용한 자동 빌드
- [X] Docker 이미지 자동 생성
- [X] GitHub Webhook 작동
- [ ] GitHub Push를 트리거로 Jenkins Pipeline 실행
- [ ] 기존 컨테이너 종료 후 신규 컨테이너 배포