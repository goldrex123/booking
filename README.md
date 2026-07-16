# Booking API

차량·부속실 예약 관리 시스템의 백엔드 REST API입니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 (Virtual Threads) |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security 6 + JWT |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA (Hibernate) |
| Documentation | Springdoc OpenAPI 3.0 |
| Build | Gradle |
| Infrastructure | Docker Compose |

## 주요 기능

- **인증**: 회원가입 / 로그인 / 로그아웃 / Access·Refresh Token 재발급
- **차량 관리**: 차량 등록·수정·상태 변경 / 가용 차량 조회
- **부속실 관리**: 부속실 등록·수정·상태 변경 / 가용 부속실 조회
- **예약**: 차량·부속실 예약 생성·수정·취소 / 캘린더용 전체 조회 / 내 예약 목록
- **관리자**: 전체 사용자 목록 조회 / 사용자 역할 변경

## 시작하기

### 사전 요구사항

- Java 21+
- Docker & Docker Compose

### 설치 및 실행

```bash
# 1. 저장소 클론
git clone https://github.com/goldrex123/booking.git
cd booking

# 2. 환경변수 파일 생성
cp .env.example .env
# .env 파일을 열어 MYSQL_ROOT_PASSWORD, MYSQL_DATABASE, JWT_SECRET 설정

# 3. 데이터베이스 시작
docker compose up -d

# 4. 애플리케이션 실행
./gradlew bootRun
```

### 환경변수

| 변수 | 설명 | 필수 |
|------|------|:----:|
| `DATABASE_URL` | MySQL 접속 URL | ✓ |
| `MYSQL_USER` | DB 사용자 | ✓ |
| `MYSQL_PASSWORD` | DB 비밀번호 | ✓ |
| `MYSQL_ROOT_PASSWORD` | MySQL root 비밀번호 (Docker 전용) | ✓ |
| `MYSQL_DATABASE` | 생성할 DB명 (Docker 전용) | ✓ |
| `JWT_SECRET` | JWT 서명 키 (32자 이상) | ✓ |

## API 문서

애플리케이션 실행 후 Swagger UI에서 전체 API 스펙을 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui.html
```

### 엔드포인트 요약

| 메서드 | 경로 | 설명 | 인증 | 역할 |
|--------|------|------|:----:|------|
| `POST` | `/api/auth/signup` | 회원가입 | ✗ | — |
| `POST` | `/api/auth/login` | 로그인 | ✗ | — |
| `POST` | `/api/auth/logout` | 로그아웃 | ✓ | — |
| `POST` | `/api/auth/refresh` | Token 재발급 | Cookie | — |
| `GET` | `/api/admin/vehicles` | 차량 목록 | ✓ | ADMIN |
| `POST` | `/api/admin/vehicles` | 차량 등록 | ✓ | ADMIN |
| `PUT` | `/api/admin/vehicles/{id}` | 차량 수정 | ✓ | ADMIN |
| `PATCH` | `/api/admin/vehicles/{id}/status` | 차량 상태 변경 | ✓ | ADMIN |
| `GET` | `/api/admin/rooms` | 부속실 목록 | ✓ | ADMIN |
| `POST` | `/api/admin/rooms` | 부속실 등록 | ✓ | ADMIN |
| `PUT` | `/api/admin/rooms/{id}` | 부속실 수정 | ✓ | ADMIN |
| `PATCH` | `/api/admin/rooms/{id}/status` | 부속실 상태 변경 | ✓ | ADMIN |
| `GET` | `/api/admin/users` | 사용자 목록 | ✓ | ADMIN |
| `PATCH` | `/api/admin/users/{id}/role` | 역할 변경 | ✓ | ADMIN |
| `GET` | `/api/vehicles/available` | 가용 차량 조회 | ✓ | USER+ |
| `GET` | `/api/rooms/available` | 가용 부속실 조회 | ✓ | USER+ |
| `GET` | `/api/reservations` | 전체 예약 조회 | ✓ | USER+ |
| `POST` | `/api/reservations` | 예약 생성 | ✓ | USER+ |
| `GET` | `/api/reservations/my` | 내 예약 조회 | ✓ | USER+ |
| `GET` | `/api/reservations/{id}` | 예약 상세 | ✓ | USER+ |
| `PUT` | `/api/reservations/{id}` | 예약 수정 | ✓ | 본인/ADMIN |
| `DELETE` | `/api/reservations/{id}` | 예약 취소 | ✓ | 본인/ADMIN |

## 홈서버 배포

### 사전 준비

홈서버에 다음이 이미 Docker Compose로 떠 있어야 합니다 (compose 프로젝트 `sky`, 네트워크 `sky_default`):
- `mysql-db` (MySQL 8.0)
- `nginx-proxy-manager` (리버스 프록시, HTTPS)

### 1. DB 최초 설정 (1회성)

`mysql-db` 컨테이너에 이 프로젝트 전용 DB·계정을 생성합니다.

```bash
docker exec -it mysql-db mysql -uroot -p
```

```sql
CREATE DATABASE booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'booking'@'%' IDENTIFIED BY '<strong-password>';
GRANT ALL PRIVILEGES ON booking.* TO 'booking'@'%';
FLUSH PRIVILEGES;
```

### 2. 서버에 `.env` 생성

프로젝트 루트에 아래 내용으로 `.env` 파일을 직접 생성합니다 (git에 커밋되지 않음).

```
DATABASE_URL=jdbc:mysql://mysql-db:3306/booking?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
MYSQL_USER=booking
MYSQL_PASSWORD=<strong-password>
JWT_SECRET=<strong-secret-32자-이상>
SPRING_PROFILES_ACTIVE=prod
```

### 2-1. `application-prod.yaml` 배치

`.gitignore`에 의해 `application-prod.yaml`은 git에 포함되지 않으므로, `git clone`만으로는 서버에 존재하지 않습니다.
로컬 개발 환경의 `src/main/resources/application-prod.yaml` 파일을 서버의 같은 경로로 복사해야 합니다.

**로컬에서 실행:**
```bash
scp src/main/resources/application-prod.yaml user@home-server:/path/to/booking/src/main/resources/
```

**서버에 `application-prod.yaml` 파일 생성:** (로컬에서 복사하지 않는 경우)

로컬의 `src/main/resources/application-prod.yaml` 파일 내용은 다음과 같습니다. 서버에 같은 경로(`src/main/resources/application-prod.yaml`)로 직접 생성하세요.

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${MYSQL_USER}
    password: ${MYSQL_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        default_batch_fetch_size: 100
    open-in-view: false

jwt:
  secret: ${JWT_SECRET}

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

logging:
  level:
    root: WARN
    sky.ch.booking: INFO
```

### 3. 최초 스키마 생성

`application-prod.yaml`은 `ddl-auto: validate`이므로 스키마가 없는 상태로 최초 기동하면 실패합니다.
최초 1회는 `SPRING_PROFILES_ACTIVE=dev`로 임시 기동해 스키마를 생성한 뒤(`ddl-auto: update`), 이후 `.env`의
`SPRING_PROFILES_ACTIVE=prod`로 되돌려 재기동하세요.

### 4. 배포

```bash
git clone https://github.com/goldrex123/booking.git
cd booking
# .env 파일 생성 (위 2번 참고)
./deploy.sh
```

### 5. NPM 연동

Nginx Proxy Manager 웹 UI(포트 81)에서 Proxy Host를 추가합니다.
- Domain: 소유 도메인
- Forward Hostname: `booking-app`
- Forward Port: `8080`
- SSL 탭에서 Let's Encrypt 인증서 발급 + Force SSL 활성화

## 프로젝트 구조

```
src/main/java/sky/ch/booking
├── common/               # 공통 응답 래퍼, 예외 클래스
├── config/               # Security, JPA, Swagger 설정
├── domain/
│   ├── admin/            # 사용자 관리 (ADMIN 전용)
│   ├── auth/             # 인증 (회원가입, 로그인, 토큰)
│   ├── reservation/      # 예약
│   ├── room/             # 부속실
│   └── vehicle/          # 차량
└── security/             # JWT 필터, UserDetails, 인증·인가 핸들러
```

## 개발

```bash
# 전체 테스트
./gradlew test

# 특정 도메인 테스트
./gradlew test --tests "sky.ch.booking.domain.reservation.*"

# 빌드
./gradlew build
```
