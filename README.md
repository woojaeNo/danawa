

  AI 기능 (Gemini API)
- **PC 견적 추천 기능**: ChatService를 통한 Gemini API 활용
  - `getAiResponse()`: 사용자 쿼리 기반 부품 추천
  - `getFullPcEstimate()`: 전체 PC 견적 추천
  - `getPcEstimateGeminiStyle()`: Gemini Test 형식 견적 추천
  
- **리뷰 요약 기능**: `summarize_reviews.py`
  - Gemini API를 사용하여 퀘이사존 리뷰 자동 요약
  - `community_reviews` 테이블의 `ai_summary` 필드에 저장
  - PartResponseDto에서 `aiSummary` 필드로 제공



 .gitignore 파일 추가
- 루트 디렉토리에 `.gitignore` 생성
- `.env` 파일 및 환경 변수 파일 제외
- Python 캐시, IDE 설정, 로그 파일 등 제외
- **영향 파일**: `.gitignore` (신규 생성)

 보안 문서 추가
- `SECURITY.md` 파일 생성
- API 키 관리 방법 안내
- 민감 정보 보호 가이드

---

 5. 문서 추가

 실행 가이드
- `DOCKER_GUIDE.md`: 단계별 Docker 실행 가이드
- Docker 명령어 및 문제 해결 방법 포함

  변경 사항 문서
- `CHANGELOG.md`: 상세 변경 사항 기록
- `COMMIT_MESSAGE.md`: GitHub 커밋 메시지 형식

---



 크롤러
-  `crawler.py` - 환경 변수 지원, 스키마 자동 생성
-  `requirements.txt` - UTF-8 인코딩으로 재저장

 백엔드
-  `webservice/src/main/resources/application.properties` - 포트 및 ddl-auto 수정
-  `webservice/src/main/java/com/danawa/webservice/service/PartService.java` - LAZY 로딩 초기화
-  `webservice/src/main/java/com/danawa/webservice/service/ChatService.java` - Map import 추가

 보안
-  `.gitignore` (신규) - .env 파일 및 민감 정보 제외
-  `SECURITY.md` (신규) - 보안 가이드

문서
-  `DOCKER_GUIDE.md` (신규) - Docker 실행 가이드
-  `CHANGELOG.md` (신규) - 변경 사항 기록
-  `COMMIT_MESSAGE.md` (신규) - 커밋 메시지 형식

---

  개선 효과

1. 환경 독립성
-  로컬 및 Docker 환경 모두에서 동작
-  환경 변수 지원으로 유연한 설정

 2. 데이터 안정성
-  테이블 재생성 방지로 데이터 보존
-  자동 스키마 생성으로 수동 작업 불필요

 3. 호환성
-  다양한 MySQL 타입(INT/BIGINT) 자동 지원
-  포트 설정 자동 조정

4. 보안
-  API 키 및 민감 정보 GitHub 커밋 방지
- 보안 가이드 문서화

5. 사용성
-  명확한 실행 가이드 제공
-  문제 해결 가이드 포함

 6. AI 기능
-  Gemini API를 통한 PC 견적 추천
-  리뷰 자동 요약 기능

---

### 크롤러 기능
- 다나와 상품 정보 수집
- 퀘이사존 리뷰 자동 수집
- 벤치마크 점수 추출
- 데이터베이스 스키마 자동 관리

### 백엔드 기능
- RESTful API 제공
- 동적 필터링 지원
- AI 기반 PC 견적 추천
- 리뷰 요약 제공

### 프론트엔드 기능
- 부품 검색 및 필터링
- 상품 비교 기능
- AI 견적 추천 UI
- 다크/라이트 모드 지원

---

##  변경 전/후 비교

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| **DB 연결** | Docker 전용 | 로컬/Docker 모두 지원 |
| **스키마 관리** | 수동 생성 필요 | 자동 생성 |
| **데이터 보존** | 백엔드 시작 시 삭제 위험 | 안전하게 보존 |
| **보안** | API 키 하드코딩 | .gitignore로 보호 |
| **문서** | 부족 | 상세한 가이드 제공 |
| **컴파일** | 오류 발생 | 정상 작동 |

---

##  주의사항

보안 관련
1. **API 키 관리**
   - 현재 `application.properties`에 API 키가 하드코딩되어 있음
   - 가능하면 환경 변수로 변경 권장
   - 이미 커밋된 키는 새로 생성 권장

2. **.env 파일**
   - 루트 디렉토리에 `.env` 파일 생성하여 사용
   - `.gitignore`에 추가되어 있어 커밋되지 않음

실행 관련
1. **Docker 실행 순서**
   - DB → 백엔드 → 프론트엔드 순서로 시작
   - 크롤러는 수동 실행

2. **포트 확인**
   - 3307: DB (호스트 포트)
   - 8080: 백엔드
   - 3000: 프론트엔드

---

 참고 문서

- `DOCKER_GUIDE.md` - Docker 실행 가이드
- `CHANGELOG.md` - 상세 변경 사항
- `COMMIT_MESSAGE.md` - GitHub 커밋 메시지
- `SECURITY.md` - 보안 가이드
- `README.md` - 프로젝트 개요

---

