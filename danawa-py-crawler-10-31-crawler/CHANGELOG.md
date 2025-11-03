# 변경 사항 (Changelog)

## 주요 개선 사항 및 버그 수정

### 🔧 크롤러 설정 개선

#### requirements.txt 인코딩 문제 해결
- **문제**: Windows에서 `requirements.txt` 파일을 읽을 때 cp949 인코딩 오류 발생
- **해결**: 파일을 UTF-8로 재저장하여 인코딩 문제 해결

#### 데이터베이스 연결 설정 개선
- **변경 전**: Docker 환경 전용 설정 (`DB_HOST='db'`, `DB_PORT='3306'`)
- **변경 후**: 로컬 및 Docker 환경 모두 지원
  - 환경 변수 지원 추가 (`DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`)
  - 기본값: `localhost:3307` (로컬 실행 시 Docker 포트 매핑 사용)
  - Docker 환경에서는 환경 변수로 자동 설정됨

#### 데이터베이스 스키마 자동 생성
- **warranty_info 컬럼 자동 추가**
  - `parts` 테이블에 `warranty_info` 컬럼이 없으면 자동으로 추가
  - 크롤러 시작 시 컬럼 존재 여부 확인 후 자동 추가

- **필수 테이블 자동 생성**
  - `part_spec` 테이블 자동 생성 (JSON 스펙 저장용)
  - `community_reviews` 테이블 자동 생성 (퀘이사존 리뷰 저장용)
  - `benchmark_results` 테이블 자동 생성 (벤치마크 점수 저장용)

- **외래키 호환성 개선**
  - `parts.id` 타입에 따라 `part_id` 타입 자동 결정 (INT/BIGINT)
  - 외래키 제약조건을 테이블 생성 후 별도로 추가하여 타입 불일치 방지

---

### 🔧 백엔드 설정 개선

#### application.properties 수정
- **변경 전**: 
  - DB 포트: `3306` (로컬 MySQL)
  - `ddl-auto=create` (테이블 재생성으로 데이터 삭제 위험)

- **변경 후**:
  - DB 포트: `3307` (Docker 포트 매핑 사용)
  - `ddl-auto=none` (기존 테이블 사용, 데이터 보존)

#### PartService 개선
- LAZY 로딩 초기화 로직 추가
- `findByFilters()` 및 `findByIds()` 메서드에서 N+1 문제 방지를 위한 초기화 추가

#### ChatService 수정
- `Map` import 누락 문제 해결
- 컴파일 오류 수정

#### AI 기능 (Gemini API)
- **PC 견적 추천 기능**: ChatService를 통한 Gemini API 활용
  - `getAiResponse()`: 사용자 쿼리 기반 부품 추천
  - `getFullPcEstimate()`: 전체 PC 견적 추천
  - `getPcEstimateGeminiStyle()`: Gemini Test 형식 견적 추천
  
- **리뷰 요약 기능**: `summarize_reviews.py`
  - Gemini API를 사용하여 퀘이사존 리뷰 자동 요약
  - `community_reviews` 테이블의 `ai_summary` 필드에 저장
  - PartResponseDto에서 `aiSummary` 필드로 제공

---

### 🐳 Docker 설정 개선

#### docker-compose.yml 개선
- **프론트엔드 API URL 수정**
  - 변경 전: `http://backend:8080` (Docker 네트워크 내부)
  - 변경 후: `http://localhost:8080` (브라우저 접근 가능)

- **백엔드 환경 변수 추가**
  - `SPRING_JPA_HIBERNATE_DDL_AUTO=none` 추가 (테이블 자동 생성 방지)

- **크롤러 서비스 설정 개선**
  - 프로필(`profiles`) 추가하여 기본 시작 시 실행되지 않도록 설정
  - 수동 실행 방식으로 변경

#### Docker 실행 가이드 추가
- `DOCKER_GUIDE.md` 파일 추가
- 단계별 실행 방법 및 문제 해결 가이드 작성

---

## 📝 변경된 파일 목록

### 크롤러 관련
- `crawler.py`
  - 환경 변수 지원 추가
  - DB 스키마 자동 생성 로직 추가
  - 테이블 및 컬럼 자동 생성 기능 추가

- `requirements.txt`
  - UTF-8 인코딩으로 재저장

### 백엔드 관련
- `webservice/src/main/resources/application.properties`
  - DB 포트: `3306` → `3307`
  - `ddl-auto`: `create` → `none`

- `webservice/src/main/java/com/danawa/webservice/service/PartService.java`
  - LAZY 로딩 초기화 로직 추가

- `webservice/src/main/java/com/danawa/webservice/service/ChatService.java`
  - `import java.util.Map;` 추가 (컴파일 오류 수정)

### Docker 관련
- `docker-compose.yml`
  - 프론트엔드 API URL 수정
  - 백엔드 환경 변수 추가
  - 크롤러 서비스 설정 개선

### 문서
- `DOCKER_GUIDE.md` (신규 추가)
  - Docker 실행 가이드
- `CHANGELOG.md` (신규 추가)
  - 변경 사항 기록

---

## 🎯 해결된 주요 문제

1. ✅ **requirements.txt 인코딩 오류** - UTF-8로 재저장하여 해결
2. ✅ **DB 연결 실패** - 포트 설정 및 환경 변수 지원으로 해결
3. ✅ **테이블/컬럼 누락 오류** - 자동 생성 로직 추가로 해결
4. ✅ **외래키 타입 불일치** - 동적 타입 확인 및 별도 추가로 해결
5. ✅ **백엔드 데이터 삭제 문제** - `ddl-auto=none` 설정으로 해결
6. ✅ **프론트엔드 API 연결 실패** - URL 수정으로 해결
7. ✅ **컴파일 오류** - import 문 추가로 해결

---

## 🚀 개선 효과

1. **환경 독립성**: 로컬 및 Docker 환경 모두에서 동작
2. **데이터 안정성**: 테이블 재생성 방지로 데이터 보존
3. **자동화**: 필요한 테이블/컬럼 자동 생성으로 수동 작업 불필요
4. **호환성**: 다양한 MySQL 타입(INT/BIGINT) 자동 지원
5. **사용성**: 명확한 실행 가이드 제공

