
# Job API Project

## 프로젝트 개요
Job API 프로젝트는 채용 공고 관리, 지원 관리, 북마크 관리 등의 기능을 제공하는 RESTful API 프로젝트입니다.

---

## 주요 기능
- **회원 관리 API** (`/auth`)
  - 회원가입
  - 로그인 (JWT 토큰 발급)
  - 토큰 갱신
  - 회원 정보 수정

- **채용 공고 API** (`/jobs`)
  - 공고 목록 조회 (페이지네이션, 필터링, 검색 지원)
  - 공고 상세 조회 (관련 공고 추천 기능 포함)

- **지원 관리 API** (`/applications`)
  - 지원하기
  - 지원 내역 조회 (상태별 필터링)
  - 지원 취소

- **북마크 관리 API** (`/bookmarks`)
  - 북마크 추가/삭제
  - 북마크 목록 조회 (페이지네이션)

---

## 프로젝트 실행 방법

### 1. **필수 요구사항**
- **Java 17** 이상 설치
- **Gradle** 설치

---

### 2. **로컬 환경에서 실행**
1. 프로젝트를 클론하거나 파일을 다운로드합니다.
   ```bash
   git clone <your-repository-url>
   cd <your-project-folder>
   ```

2. Gradle을 사용하여 라이브러리를 설치합니다.
   ```bash
   ./gradlew build
   ```

3. Spring Boot 애플리케이션을 실행합니다.
   ```bash
   ./gradlew bootRun
   ```

4. 로컬 환경에서 API에 접근하려면 Postman에서 다음 URL을 사용합니다:
   ```
   http://localhost:8080
   ```

---

### 3. **배포된 환경에서 실행**
프로젝트는 이미 배포된 상태로, 아래 방법을 통해 서버에서 실행 가능합니다.

#### 1. SSH 접속
SSH를 통해 서버에 접속합니다.
```bash
ssh ubuntu@113.198.66.75 -p 19217
```

#### 2. 프로젝트 디렉토리 이동
```bash
cd Projects
```

#### 3. 프로젝트 실행
```bash
java -jar jobAPI-0.0.1-SNAPSHOT.jar
```

---

## API 테스트
Postman을 통해 배포된 API에 접근할 수 있습니다:
```
http://113.198.66.75:10217
```

---

## 주요 기술 스택
- **Backend**: Spring Boot
- **Build Tool**: Gradle
- **Database**: MySQL
- **API Testing**: Postman

---

## API 문서
API 명세는 Swagger를 통해 확인할 수 있습니다. Swagger 문서는 다음 주소에서 접근 가능합니다:
```
http://113.198.66.75:10217/swagger-ui/index.html
```

---

## API 명세서 (한글)
### 회원 관리 API (`/auth`)
- **회원가입** (`POST /auth/register`): 이메일 검증, 비밀번호 암호화, 사용자 정보 저장
- **로그인** (`POST /auth/login`): JWT 토큰 발급 및 에러 처리
- **토큰 갱신** (`POST /auth/refresh`): Refresh 토큰 검증 및 새로운 Access 토큰 발급
- **회원 정보 수정** (`PUT /auth/profile`): 인증 후 사용자 정보 수정

### 채용 공고 API (`/jobs`)
- **공고 목록 조회** (`GET /jobs`): 페이지네이션 처리 (페이지 크기: 20), 필터링 및 검색 지원
- **공고 상세 조회** (`GET /jobs/{id}`): 공고 상세 정보 제공 및 관련 공고 추천 기능

### 지원 관리 API (`/applications`)
- **지원하기** (`POST /applications`): 지원 정보 저장, 이력서 첨부 (선택)
- **지원 내역 조회** (`GET /applications`): 사용자별 지원 내역, 상태별 필터링 및 정렬
- **지원 취소** (`DELETE /applications/{id}`): 상태 업데이트 및 인증 확인

### 북마크 API (`/bookmarks`)
- **북마크 추가/삭제** (`POST /bookmarks`): 사용자별 북마크 저장/삭제
- **북마크 목록 조회** (`GET /bookmarks`): 사용자별 북마크 조회 (최신순 정렬)

---

## 문의
- 프로젝트에 관한 문의는 [email@example.com](mailto:email@example.com)으로 연락주시기 바랍니다.
