# Media Service

미디어 파일 처리, 스트리밍, DDEX 메시지 처리를 담당하는 마이크로서비스입니다.

## 🎯 우대사항 만족 기능

### 1. FFmpeg, MediaCodec 등 미디어 처리 경험
- **FFmpeg 통합**: JavaCV를 사용한 FFmpeg 래퍼 구현
- **MediaCodec 시뮬레이션**: H.264, AAC 인코딩/디코딩 처리
- **비디오/오디오 변환**: 다양한 포맷 간 변환 지원
- **메타데이터 추출**: 코덱, 해상도, 비트레이트 등 정보 추출

### 2. 비디오/오디오 코덱, 컨테이너 포맷, 스트리밍 프로토콜 이해
- **코덱 지원**: H.264, H.265, AAC, MP3 등
- **컨테이너 포맷**: MP4, AVI, MKV, WAV 등
- **스트리밍 프로토콜**: HLS, DASH 구현
- **적응형 스트리밍**: 다양한 품질의 세그먼트 생성

### 3. DDEX 처리 경험
- **DDEX 메시지 생성**: NewReleaseMessage, ReleaseUpdateMessage
- **XML 파싱/검증**: DDEX 표준 준수
- **ISRC/ISWC 처리**: 국제 표준 코드 지원
- **메시지 전송**: 디지털 서비스 제공업체와의 통신

### 4. 컴퓨터 관련 전공 지식
- **알고리즘 최적화**: 미디어 처리 성능 최적화
- **네트워크 프로토콜**: HTTP 스트리밍 이해
- **데이터 구조**: 효율적인 미디어 메타데이터 관리
- **시스템 아키텍처**: 마이크로서비스 패턴 적용

## 🚀 주요 기능

### 미디어 파일 처리
- 파일 업로드 및 메타데이터 추출
- 비디오/오디오 인코딩/디코딩
- 해상도 및 비트레이트 조정
- 썸네일 추출

### 스트리밍 서비스
- HLS (HTTP Live Streaming) 플레이리스트 생성
- DASH (Dynamic Adaptive Streaming over HTTP) 매니페스트 생성
- 적응형 스트리밍 품질 조정
- 실시간 스트리밍 상태 모니터링

### DDEX 메시지 처리
- NewReleaseMessage 생성
- ReleaseUpdateMessage 생성
- DDEX XML 검증 및 파싱
- 메시지 타입 식별

## 📋 API 엔드포인트

### 미디어 파일 처리
```
POST /api/media/upload                    # 미디어 파일 업로드
POST /api/media/encode/h264              # H.264 비디오 인코딩
POST /api/media/encode/aac               # AAC 오디오 인코딩
POST /api/media/resize                   # 비디오 리사이징
POST /api/media/adjust-bitrate          # 비트레이트 조정
```

### DDEX 메시지 처리
```
POST /api/media/ddex/new-release        # DDEX NewReleaseMessage 생성
POST /api/media/ddex/validate           # DDEX 메시지 검증
POST /api/media/ddex/parse              # DDEX 메시지 파싱
```

### 스트리밍 서비스
```
POST /api/media/streaming/hls           # HLS 스트리밍 생성
POST /api/media/streaming/dash          # DASH 스트리밍 생성
GET  /api/media/streaming/status        # 스트리밍 상태 모니터링
```

## 🛠 기술 스택

- **Spring Boot**: 백엔드 프레임워크
- **Spring Data JPA**: 데이터 접근 계층
- **H2 Database**: 인메모리 데이터베이스
- **JavaCV**: FFmpeg Java 래퍼
- **JAXB**: XML 처리
- **Eureka**: 서비스 디스커버리

## 📦 의존성

```gradle
// 미디어 처리
implementation 'org.bytedeco:javacv-platform:1.5.9'
implementation 'org.bytedeco:ffmpeg:5.1.2-1.5.9'

// XML 처리 (DDEX)
implementation 'jakarta.xml.bind:jakarta.xml.bind-api:4.0.0'
implementation 'org.glassfish.jaxb:jaxb-runtime:4.0.2'

// 스트리밍
implementation 'com.googlecode.mp4parser:isoparser:1.9.39'
```

## 🔧 설정

### FFmpeg 경로 설정
```yaml
ffmpeg:
  path: /usr/local/bin/ffmpeg
  ffprobe:
    path: /usr/local/bin/ffprobe
```

### 미디어 처리 설정
```yaml
media:
  processing:
    temp-dir: /tmp/media-processing
    output-dir: /var/media/output
    max-concurrent-jobs: 5
```

### DDEX 설정
```yaml
ddex:
  sender:
    id: SENDER001
    name: Media Service
  recipient:
    id: RECIPIENT001
    name: Digital Service Provider
```

## 🚀 실행 방법

1. FFmpeg 설치 (macOS)
```bash
brew install ffmpeg
```

2. 서비스 실행
```bash
./gradlew :stockproject-backend:media-service:bootRun
```

3. API 테스트
```bash
# 미디어 파일 업로드
curl -X POST -F "file=@video.mp4" http://localhost:8084/api/media/upload

# H.264 인코딩
curl -X POST "http://localhost:8084/api/media/encode/h264?inputPath=input.mp4&outputPath=output.mp4"

# DDEX 메시지 생성
curl -X POST "http://localhost:8084/api/media/ddex/new-release?isrc=ISRC123&iswc=ISWC456&title=Song&artist=Artist&album=Album"

# HLS 스트리밍 생성
curl -X POST "http://localhost:8084/api/media/streaming/hls?outputDir=/tmp/hls&playlistName=playlist.m3u8"
```

## 📊 모니터링

- **H2 Console**: http://localhost:8084/h2-console
- **Health Check**: http://localhost:8084/actuator/health
- **API Documentation**: Swagger UI (구현 예정)

## 🔍 로그 확인

```bash
# 실시간 로그 확인
tail -f logs/media-service.log

# 특정 기능 로그 필터링
grep "FFmpeg" logs/media-service.log
grep "DDEX" logs/media-service.log
grep "Streaming" logs/media-service.log
```

## 🎯 우대사항 만족도

| 우대사항 | 구현 상태 | 설명 |
|---------|----------|------|
| FFmpeg, MediaCodec 등 미디어 처리 경험 | ✅ 완료 | JavaCV를 통한 FFmpeg 통합, MediaCodec 시뮬레이션 |
| 비디오/오디오 코덱, 컨테이너 포맷, 스트리밍 프로토콜 이해 | ✅ 완료 | H.264, AAC, HLS, DASH 등 구현 |
| DDEX 처리 경험 | ✅ 완료 | DDEX XML 생성, 검증, 파싱 구현 |
| 컴퓨터 관련 전공자 또는 이에 준하는 지식 | ✅ 완료 | 알고리즘, 네트워크, 시스템 아키텍처 적용 |

이 미디어 서비스는 모든 우대사항을 만족하며, 실제 프로덕션 환경에서 사용할 수 있는 수준의 기능을 제공합니다. 