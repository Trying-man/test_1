# 늑대와 양 미니게임 실행 안내

이 프로젝트는 Android용 늑대-양 숨바꼭질 게임입니다. 네트워크가 제한된 환경에서는 자동으로 SDK를 내려받지 못하므로, 아래 절차에 따라 로컬에 설치된 Android SDK를 사용하도록 설정해야 합니다.

## 준비 사항
- 미리 내려받아 둔 Android SDK 경로 (예: `/workspace/android-sdk`)
- 필요한 플랫폼과 빌드 도구: `platforms;android-31`, `build-tools;31.0.0`

## 환경 설정 방법
1. Android SDK 위치를 지정합니다.
   - `local.properties` 파일을 프로젝트 루트에 생성하고 아래와 같이 작성합니다.
     ```
     sdk.dir=/경로/설치된-android-sdk
     ```
   - 또는 셸 환경 변수로 지정합니다.
     ```bash
     export ANDROID_SDK_ROOT=/경로/설치된-android-sdk
     ```
2. 네트워크 프록시가 외부 다운로드를 차단하는 경우, SDK와 빌드 도구를 로컬에 사전 설치해야 합니다.

## 빌드 및 테스트
```bash
./gradlew test
```
SDK가 올바르게 지정되지 않으면 `SDK location not found` 오류가 발생하므로 먼저 SDK 경로를 설정하세요.
