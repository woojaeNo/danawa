# Docker 실행 가이드

## 실행 순서 (처음부터 끝까지)

### 1단계: Docker Desktop 실행 확인
- Docker Desktop이 실행 중인지 확인
- 실행되지 않았다면 Docker Desktop 실행

### 2단계: 프로젝트 디렉토리로 이동
```powershell
cd danawa-py-crawler-10-31-crawler
```

### 3단계: 기본 서비스 빌드 및 실행 (DB, 백엔드, 프론트엔드)
```powershell
docker-compose up -d --build
```

**실행되는 서비스:**
- **DB (MySQL)**: `localhost:3307`
- **백엔드 (Spring Boot)**: `localhost:8080`
- **프론트엔드 (React)**: `localhost:3000`

**예상 시간:** 처음 빌드 시 5-10분 (의존성 다운로드 포함)

### 4단계: DB 준비 대기
```powershell
docker-compose logs -f db
```
**확인 사항:** 
- `ready for connections` 메시지가 나올 때까지 대기 (약 10-20초)
- `Ctrl+C`로 로그 종료

### 5단계: 크롤러 실행 (데이터 수집)
```powershell
docker-compose run --rm crawler python crawler.py
```

**예상 시간:** 카테고리 수에 따라 10-30분

**완료 확인:**
- `모든 카테고리 데이터 수집을 완료했습니다.` 메시지 확인

### 6단계: 웹 브라우저에서 확인
- **프론트엔드**: http://localhost:3000
- **백엔드 API**: http://localhost:8080/api/parts?category=CPU

---

## 주요 명령어

### 서비스 관리
```powershell
# 모든 서비스 시작
docker-compose up -d

# 모든 서비스 중지
docker-compose down

# 모든 서비스 중지 및 볼륨 삭제 (데이터 삭제)
docker-compose down -v

# 특정 서비스만 재시작
docker-compose restart backend

# 로그 확인
docker-compose logs -f backend    # 백엔드 로그
docker-compose logs -f frontend  # 프론트엔드 로그
docker-compose logs -f db        # DB 로그
```

### 크롤러 관련
```powershell
# 크롤러 실행 (데이터 수집)
docker-compose run --rm crawler python crawler.py

# 크롤러 컨테이너에 접속 (디버깅용)
docker-compose run --rm crawler bash
```

### 상태 확인
```powershell
# 실행 중인 컨테이너 확인
docker-compose ps

# 서비스 상태 확인
docker-compose logs backend
docker-compose logs frontend
docker-compose logs db
```

---

## 문제 해결

### 빌드 실패
```powershell
# 캐시 없이 다시 빌드
docker-compose build --no-cache
docker-compose up -d
```

### 포트 충돌
- 다른 서비스가 3307, 8080, 3000 포트를 사용 중인 경우
- `docker-compose.yml`에서 포트 번호 변경

### 데이터가 안 보임
1. 크롤러가 완료되었는지 확인
2. 백엔드 로그 확인: `docker-compose logs backend`
3. 브라우저 개발자 도구(F12) → Network 탭에서 API 요청 확인

### 컨테이너 재시작
```powershell
# 특정 서비스 재시작
docker-compose restart backend

# 모든 서비스 재시작
docker-compose restart
```

---

## 전체 실행 한 줄 명령어 (순서대로)
```powershell
# 1. 서비스 실행
docker-compose up -d --build

# 2. DB 준비 대기 (약 20초)
timeout /t 20

# 3. 크롤러 실행
docker-compose run --rm crawler python crawler.py
```

---

## 참고사항

- **첫 실행 시**: 빌드 시간이 오래 걸릴 수 있습니다 (Maven, npm 의존성 다운로드)
- **데이터 보존**: `docker-compose down -v`를 실행하면 DB 데이터가 삭제됩니다
- **로그 확인**: 문제 발생 시 `docker-compose logs` 명령으로 로그를 확인하세요


