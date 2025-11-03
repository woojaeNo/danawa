# 보안 가이드

## 🔒 API 키 및 민감 정보 관리

### ❌ 절대 커밋하지 말아야 할 것들
- `.env` 파일
- API 키 (Gemini API Key 등)
- 데이터베이스 비밀번호
- 인증 토큰

### ✅ 안전한 설정 방법

#### 1. .env 파일 사용 (권장)
```bash
# .env.example을 복사하여 .env 생성
cp .env.example .env

# .env 파일에 실제 API 키 입력
GOOGLE_API_KEY=실제_API_키
```

#### 2. 환경 변수로 설정
```bash
# Windows PowerShell
$env:GOOGLE_API_KEY="실제_API_키"

# Linux/Mac
export GOOGLE_API_KEY="실제_API_키"
```

#### 3. application.properties에서 API 키 제거
현재 `application.properties`에 하드코딩된 API 키를 제거하고 환경 변수로 대체하는 것을 권장합니다.

---

## 📝 현재 상태

### ⚠️ 주의사항
`webservice/src/main/resources/application.properties` 파일에 Gemini API 키가 하드코딩되어 있습니다:
```
gemini.api.key=AIzaSyDf2SxfFR1x5XB2kd030bWMWESZ4Fgv-xM
```

**권장 조치:**
1. 이 값을 환경 변수로 변경하거나
2. .env 파일로 관리하도록 수정

---

## 🛡️ .gitignore 설정

루트 디렉토리에 `.gitignore` 파일을 생성하여 다음 항목들이 커밋되지 않도록 설정했습니다:
- `.env`
- `.env.local`
- `.env.*.local`

---

## 🔐 추가 보안 권장사항

1. **API 키 로테이션**: 정기적으로 API 키를 변경하세요
2. **권한 제한**: API 키에 최소한의 권한만 부여하세요
3. **리뷰**: Pull Request 시 민감 정보가 포함되지 않았는지 확인하세요

