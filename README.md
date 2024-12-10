
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
- **MariaDB** 설치

---

### 2. **로컬 환경에서 실행**
1. 프로젝트를 클론하거나 파일을 다운로드합니다.
   ```bash
   git clone https://www.github.com/eecczz/jobAPI
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
xshell에서 세션을 추가하여,
프로토콜은 SSH,
호스트는 113.198.66.75,
포트번호는 19217 로 설정한 뒤, 사용자 인증탭에서 Public Key 방법에 체크하고,
설정으로 들어가 pem 파일을 추가해줍니다. 끝으로 SFTP탭에서 프로젝트 폴더의
하위폴더 build\libs 를 로컬 폴더로 경로설정을 하고 세션을 연결해줍니다.

#### 2. 프로젝트 디렉토리 이동
```bash
cd Projects
```

#### 3. MariaDB 설치 및 설정
1. MariaDB를 설치합니다.
   ```bash
   sudo apt update
   sudo apt install mariadb-server
   ```

2. 포트를 3000으로 변경하고 외부 접속을 허용합니다:
   ```bash
   sudo nano /etc/mysql/mariadb.conf.d/50-server.cnf
   ```
   `[mysqld]` 아래에 다음 내용을 추가합니다:
   ```
   port = 3000
   bind-address = 0.0.0.0
   ```
   설정 후 MariaDB 서비스를 재시작합니다:
   ```bash
   sudo systemctl restart mariadb
   ```

3. 사용자와 데이터베이스를 생성합니다:
   ```bash
   sudo mysql -u root
   CREATE DATABASE saramin;
   CREATE USER 'jobuser'@'%' IDENTIFIED BY 'hjh75327!@';
   GRANT ALL PRIVILEGES ON saramin.* TO 'jobuser'@'%';
   FLUSH PRIVILEGES;
   exit;
   ```

#### 4. 프로젝트 실행
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
- **Database**: MariaDB
- **API Testing**: Postman

---

## API 문서
API 명세는 Swagger를 통해 확인할 수 있습니다. Swagger 문서는 다음 주소에서 접근 가능합니다:
```
http://113.198.66.75:10217/swagger-ui/index.html
```

---

## 문의
- 프로젝트에 관한 문의는 [swh06084@gmail.com](mailto:swh06084@gmail.com)으로 연락주시기 바랍니다.
