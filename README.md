# Tonefit Server

Tonefit 서비스의 백엔드 시스템입니다. Java 21과 Spring Boot 4를 기반으로 구축되었습니다.

## 🏛 Architecture & Package Structure

이 프로젝트는 **도메인 중심 설계(Domain-Driven Design)**와 **관심사 분리(Separation of Concerns)** 원칙을 따릅니다. 모든 코드는 크게 `core`와 `domain` 패키지로 구분됩니다.

### 1. core (Technical Infrastructure)
비즈니스 로직에 종속되지 않는 기술적 인프라와 공통 규격을 관리합니다.
- **core.config**: 애플리케이션 전역 설정 (Security, JPA Auditing 등)
- **core.dto**: 전역 공통 응답 규격 (`ApiResponse`)
- **core.enums**: 공통으로 사용되는 Enum (ErrorType, UserStatus 등)
- **core.exception**: 전역 예외 처리 클래스 및 핸들러
- **core.security**: 인증/인가 관련 핵심 로직 (JWT Provider, Filter)

### 2. domain (Business Logic)
서비스의 핵심 비즈니스 기능과 데이터 모델을 담습니다. 각 기능별로 하위 패키지를 구성합니다.
- **domain.user**: 사용자 데이터 모델(Entity) 및 저장소(Repository)
- **domain.auth**: 회원가입, 로그인 등 인증 비즈니스 서비스 및 API 컨트롤러
- **domain.xxxx**: 향후 추가될 비즈니스 도메인들 (운동, 식단 등)

### 💡 설계 원칙
- **기능 기반 패키징**: `service`, `repository`와 같은 계층형 패키징 대신, 도메인(기능)별로 관련 클래스들을 응집시켜 관리합니다.
- **계층 간 의존성**: `domain`은 `core`에 의존할 수 있으나, `core`는 특정 `domain`의 내부 비즈니스 로직을 알아서는 안 됩니다.
- **불변 객체 활용**: DTO 및 공통 응답 규격은 Java 21의 `record`를 적극 활용하여 데이터의 불변성을 보장합니다.

---

## 🚀 시작하기

### 환경 설정
- **Java**: 21
- **Database**: PostgreSQL 16 (Docker)
- **Build**: Gradle

### 실행 방법
1. DB 컨테이너 실행: `docker compose up -d db`
2. 애플리케이션 실행: `./gradlew bootRun` (로컬) 또는 `docker compose up --build -d app` (도커)
