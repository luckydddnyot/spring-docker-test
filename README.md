# Cloud Run 연습 키트 🚀

회사 코드와 분리된 **학습용 더미 Spring Boot 앱**입니다.
도커 → GCP Cloud Run 배포를 처음부터 끝까지 연습하는 용도예요.

> ⚠️ 이 폴더는 회사 repo가 아닙니다. 개인 GitHub에 올려 연습하세요. 회사 코드/데이터는 넣지 마세요.

## 구성
- `pom.xml`, `src/...` : HTTP 엔드포인트 하나짜리 최소 Spring Boot 앱
- `application.properties` : `server.port=${PORT:8080}` ← **Cloud Run 핵심** (PORT 환경변수로 listen)
- `Dockerfile` : 멀티스테이지 (빌드 → JRE 실행)

---

## 0. 사전 준비

1. **Google 계정** (개인)
2. **GCP 프로젝트 생성** + **결제 계정 연결**
   - 콘솔: https://console.cloud.google.com → 프로젝트 만들기
   - 결제 연결(신용카드) 필요하지만, 신규 가입 시 **$300 크레딧**으로 커버됨. 수동 유료전환 안 하면 자동청구 안 됨.
3. **gcloud CLI 설치** (Google Cloud SDK)
   - https://cloud.google.com/sdk/docs/install (Windows 설치 후 터미널 재시작)
4. 로그인 & 프로젝트 지정
   ```bash
   gcloud auth login
   gcloud config set project <프로젝트ID>
   gcloud config set run/region asia-northeast3   # 서울 리전
   ```
5. 필요한 API 켜기
   ```bash
   gcloud services enable run.googleapis.com artifactregistry.googleapis.com cloudbuild.googleapis.com
   ```

---

## 경로 A — 가장 쉬운 길 (소스에서 바로 배포)

도커 명령 몰라도 됨. Cloud Run이 알아서 빌드(이 폴더의 Dockerfile 사용)해서 배포해줍니다.

```bash
# 이 폴더(C:\workspace\cloudrun-practice)에서 실행
gcloud run deploy demo --source . --allow-unauthenticated
```

- 처음 한 번은 Artifact Registry 저장소 생성 여부를 물어봄 → `y`
- 끝나면 **Service URL** 이 출력됨 → 브라우저로 열면 `Hello from Cloud Run!` 확인 ✅

---

## 경로 B — 도커를 직접 만지는 길 (학습 추천)

도커 빌드/실행/푸시를 손으로 해보며 원리를 익히는 코스. (도커 설치 필요)

```bash
# 1) 로컬에서 이미지 빌드
docker build -t demo .

# 2) 로컬에서 실행해보기 (http://localhost:8080)
docker run -p 8080:8080 demo

# 3) Artifact Registry 저장소 만들기 (최초 1회)
gcloud artifacts repositories create my-repo \
  --repository-format=docker --location=asia-northeast3

# 4) 도커가 GCP 레지스트리에 인증하도록 설정 (최초 1회)
gcloud auth configure-docker asia-northeast3-docker.pkg.dev

# 5) 이미지에 GCP 주소로 태그 달고 푸시
docker tag demo asia-northeast3-docker.pkg.dev/<프로젝트ID>/my-repo/demo
docker push asia-northeast3-docker.pkg.dev/<프로젝트ID>/my-repo/demo

# 6) 그 이미지로 Cloud Run 배포
gcloud run deploy demo \
  --image asia-northeast3-docker.pkg.dev/<프로젝트ID>/my-repo/demo \
  --allow-unauthenticated
```

---

## 확인 & 로그
```bash
gcloud run services list                 # 배포된 서비스 목록/URL
gcloud run services logs read demo        # 로그 보기
```

## 비용 걱정 줄이기
- Cloud Run은 **요청 없으면 0으로 스케일** → 유휴 시 사실상 과금 0
- 무료 등급(월 200만 요청 등) + $300 크레딧으로 연습은 충분
- 다 놀았으면 정리:
  ```bash
  gcloud run services delete demo
  ```

---

## 자주 만나는 함정
- **포트**: 앱이 8080(=PORT)으로 listen 안 하면 배포는 되는데 "컨테이너가 시작 안 됨" 에러. → `application.properties`의 `server.port=${PORT:8080}` 필수.
- **--allow-unauthenticated 빼먹음**: 그러면 URL 열 때 403. 연습용은 붙이는 게 편함.
- **리전 불일치**: 이미지 푸시 리전과 배포 리전이 다르면 헷갈림. 둘 다 `asia-northeast3`로 통일.

## 다음 단계 (익숙해지면)
- GitHub Actions로 push → 자동 빌드 → Cloud Run 배포 (CD) 연결
- 환경변수/시크릿 주입, 커스텀 도메인 연결
