# Spring Boot REST Docs + Swagger UI Example

[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-9.8.2-02303A?logo=gradle&logoColor=white)](https://gradle.org)
[![Spring REST Docs](https://img.shields.io/badge/Spring%20REST%20Docs-4.0-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-restdocs)
[![Swagger UI](https://img.shields.io/badge/Swagger%20UI-springdoc%203.1.1-85EA2D?logo=swagger&logoColor=black)](https://springdoc.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

Spring REST Docs 테스트 코드로 OpenAPI 3 스펙을 생성하고, 그 스펙을 Swagger UI로 보여주는 예제입니다.

컨트롤러에 Swagger 어노테이션을 붙이지 않습니다. 문서는 테스트에서 만들어지기 때문에, 테스트가 통과하면 문서와 실제 API 응답이 일치한다는 것이 보장됩니다.

## 기술 스택

| 항목 | 버전 |
| --- | --- |
| Java | 25 (Gradle toolchain) |
| Spring Boot | 4.1.1 |
| Gradle | 9.8.2 (wrapper) |
| Spring REST Docs | 4.0.x |
| [restdocs-api-spec](https://github.com/ePages-de/restdocs-api-spec) | 0.20.2 |
| springdoc-openapi (Swagger UI) | 3.1.1 |
| Jackson | 3.x (`tools.jackson`) |

> Spring Boot 3.x 기반 코드는 [`spring-boot-3x`](https://github.com/Pink-Spider/springboot-restdoc-swagger-ui-example/tree/spring-boot-3x) 브랜치에 있습니다.

## 동작 방식

```
ExampleControllerTest  ──(MockMvc + REST Docs)──▶  build/generated-snippets
                                                          │
                                       ./gradlew openapi3 ▼
                         build/api-spec/restdoc-swagger-ui-api.json
                                                          │
                                        ./gradlew copyJson ▼
                         src/main/resources/restdocs/restdoc-swagger-ui-api.json
                                                          │
                              /restdocs/** 정적 리소스로 노출 ▼
                                   Swagger UI (/swagger-ui/index.html)
```

1. 컨트롤러 테스트에서 `MockMvcRestDocumentationWrapper.document(...)`와 `resource(ResourceSnippetParameters...)`로 요약, 설명, 태그, 경로 파라미터, 요청/응답 필드를 기술합니다.
2. `openapi3` 태스크가 테스트에서 생성된 스니펫을 모아 OpenAPI 3 JSON을 만듭니다.
3. `build`가 끝나면 `copyJson` 태스크가 실행되어 생성된 JSON을 `src/main/resources/restdocs/`로 복사합니다. 이 파일은 저장소에 커밋되어 있습니다.
4. `WebConfig`가 `/restdocs/**` 경로로 해당 JSON을 노출하고, `application.yml`의 `springdoc.swagger-ui.url`이 Swagger UI가 이 파일을 읽도록 지정합니다.

## 실행 방법

```bash
# 빌드 + 테스트 + OpenAPI 스펙 생성/복사
./gradlew build

# 애플리케이션 실행 (port 8082)
./gradlew bootRun
```

- Swagger UI: http://localhost:8082/swagger-ui/index.html
- 생성된 스펙: http://localhost:8082/restdocs/restdoc-swagger-ui-api.json

JDK 25가 설치되어 있지 않아도 Gradle toolchain(foojay resolver)이 자동으로 내려받습니다.

### 자주 쓰는 명령

```bash
./gradlew test                                   # 전체 테스트
./gradlew test --tests '*ExampleControllerTest'  # 특정 테스트 클래스
./gradlew openapi3                               # 스펙만 생성 (테스트 선행)
./gradlew copyJson                               # 스펙 생성 후 src/main/resources/restdocs 로 복사
./gradlew clean                                  # build/ 와 함께 src/main/resources/restdocs/*.json 도 삭제
```

## API 추가 시 참고

- 테스트 요청은 `RestDocumentationRequestBuilders`로 만들어야 경로 파라미터(`parameterWithName`)를 문서화할 수 있습니다.
- 응답 필드를 하나라도 빠뜨리거나 실제 응답에 없는 필드를 기술하면 테스트가 실패합니다. 응답은 `ApiResult` 래퍼(`code`, `message`, `value.*`) 전체를 기준으로 기술합니다.
- API나 테스트를 변경한 뒤에는 `./gradlew build`를 실행하고, 갱신된 `src/main/resources/restdocs/restdoc-swagger-ui-api.json`을 함께 커밋합니다.

## Spring Boot 3 → 4 마이그레이션 요약

| 변경 | Boot 3.x | Boot 4.x |
| --- | --- | --- |
| Web 스타터 | `spring-boot-starter-web` | `spring-boot-starter-webmvc` |
| 테스트 스타터 | `spring-boot-starter-test` | `spring-boot-starter-webmvc-test`, `spring-boot-starter-restdocs` |
| `@WebMvcTest`, `@AutoConfigureMockMvc` | `o.s.boot.test.autoconfigure.web.servlet` | `o.s.boot.webmvc.test.autoconfigure` |
| `@AutoConfigureRestDocs` | `o.s.boot.test.autoconfigure.restdocs` | `o.s.boot.restdocs.test.autoconfigure` |
| Jackson | `com.fasterxml.jackson.databind.ObjectMapper` | `tools.jackson.databind.json.JsonMapper` (Jackson 3) |
| restdocs-api-spec | 0.17.1 | 0.20.2 |
| springdoc-openapi | 2.2.0 | 3.1.1 |

`spring-restdocs-mockmvc`는 restdocs-api-spec이 runtime 스코프로만 가져오기 때문에 `testImplementation`으로 직접 선언해야 합니다.

## License

[MIT](LICENSE)
