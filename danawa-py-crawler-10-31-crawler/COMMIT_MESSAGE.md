# GitHub 커밋 메시지

## 주요 변경 사항 요약

```
fix: 크롤러 및 백엔드 설정 개선 및 버그 수정

주요 변경 사항:
- 크롤러 DB 연결 설정 개선 (로컬/Docker 환경 지원)
- 데이터베이스 스키마 자동 생성 기능 추가
- 백엔드 설정 개선 (데이터 보존)
- Docker 설정 최적화
- 컴파일 오류 수정

BREAKING CHANGE: 없음
```

---

## 상세 커밋 메시지

```
fix: 크롤러 및 백엔드 설정 개선 및 버그 수정

크롤러 개선:
- requirements.txt 인코딩 문제 해결 (cp949 → UTF-8)
- DB 연결 설정 개선: 환경 변수 지원 추가, 로컬/Docker 호환
- 데이터베이스 스키마 자동 생성 기능 추가
  * warranty_info 컬럼 자동 추가
  * part_spec, community_reviews, benchmark_results 테이블 자동 생성
  * 외래키 타입 호환성 개선 (INT/BIGINT 자동 감지)

백엔드 개선:
- application.properties: DB 포트 3306 → 3307
- ddl-auto: create → none (데이터 보존)
- PartService: LAZY 로딩 초기화 추가 (N+1 문제 방지)
- ChatService: Map import 누락 수정 (컴파일 오류 해결)

AI 기능 (Gemini API):
- ChatService: Gemini API를 사용한 PC 견적 추천 기능
  * getAiResponse(): 사용자 쿼리 기반 부품 추천
  * getFullPcEstimate(): 전체 PC 견적 추천
  * getPcEstimateGeminiStyle(): Gemini Test 형식 견적 추천
- summarize_reviews.py: 퀘이사존 리뷰 AI 자동 요약 스크립트
  * community_reviews.ai_summary 필드에 저장
  * PartResponseDto를 통해 프론트엔드에서 aiSummary로 제공

Docker 설정:
- 프론트엔드 API URL 수정 (backend:8080 → localhost:8080)
- 백엔드 환경 변수 추가 (SPRING_JPA_HIBERNATE_DDL_AUTO=none)
- 크롤러 서비스 프로필 설정 추가

문서:
- DOCKER_GUIDE.md 추가 (실행 가이드)
- CHANGELOG.md 추가 (변경 사항 기록)

해결된 이슈:
- #1 requirements.txt 인코딩 오류
- #2 DB 연결 실패 (포트 불일치)
- #3 테이블/컬럼 누락 오류
- #4 외래키 타입 불일치
- #5 백엔드 시작 시 데이터 삭제 문제
- #6 프론트엔드 API 연결 실패
- #7 컴파일 오류 (Map import 누락)

Refs: 프로젝트 초기 설정 및 환경 호환성 개선
```

---

## 개별 커밋으로 나눌 경우

### 1. 크롤러 설정 개선
```
fix(crawler): DB 연결 설정 개선 및 스키마 자동 생성 추가

- 환경 변수 지원 추가 (DB_HOST, DB_PORT 등)
- warranty_info 컬럼 자동 추가 기능
- part_spec, community_reviews, benchmark_results 테이블 자동 생성
- 외래키 타입 호환성 개선 (INT/BIGINT 자동 감지)
- requirements.txt 인코딩 문제 해결 (UTF-8)

Closes #1, #2, #3, #4
```

### 2. 백엔드 설정 개선
```
fix(backend): 데이터 보존 및 성능 개선

- application.properties: DB 포트 3306 → 3307
- ddl-auto: create → none (테이블 재생성 방지)
- PartService: LAZY 로딩 초기화 추가 (N+1 문제 방지)
- ChatService: Map import 누락 수정

Closes #5, #7
```

### 3. Docker 설정 개선
```
fix(docker): Docker Compose 설정 최적화

- 프론트엔드 API URL 수정 (브라우저 접근 가능)
- 백엔드 환경 변수 추가 (ddl-auto=none)
- 크롤러 서비스 프로필 설정 추가

Closes #6
```

### 4. 문서 추가
```
docs: Docker 실행 가이드 및 변경 사항 문서 추가

- DOCKER_GUIDE.md: 단계별 Docker 실행 가이드
- CHANGELOG.md: 주요 변경 사항 기록

Refs: 프로젝트 문서화
```

---

## Conventional Commits 형식

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type 종류:
- `fix`: 버그 수정
- `feat`: 새로운 기능
- `docs`: 문서 변경
- `style`: 코드 포맷팅
- `refactor`: 코드 리팩토링
- `test`: 테스트 추가/수정
- `chore`: 빌드 과정 또는 보조 도구 변경

### 예시:
```
fix(crawler): DB 연결 설정 및 스키마 자동 생성 개선

- 환경 변수 지원 추가로 로컬/Docker 환경 호환
- 테이블 및 컬럼 자동 생성 기능 추가
- 외래키 타입 호환성 개선

BREAKING CHANGE: 없음
```

