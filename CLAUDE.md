# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

A small example of generating an OpenAPI 3 spec from **Spring REST Docs tests** (via `com.epages.restdocs-api-spec`) and showing it with Swagger UI. The API docs come from controller tests, not from annotations on the controllers. Spring Boot 4.1 (Jackson 3), Java 25 toolchain (auto-provisioned via foojay), Gradle 9 wrapper. Comments and docs are partly in Korean. The Boot 3.x version of the code lives on the `spring-boot-3x` branch.

## Commands

```bash
./gradlew build                      # compile + test, then (finalizedBy) copyJson → regenerates the OpenAPI spec
./gradlew test                       # run all tests
./gradlew test --tests 'io.pinkspider.restdocswaggerui.example.controller.ExampleControllerTest'
./gradlew test --tests '*ExampleControllerTest.example01'   # single test method
./gradlew openapi3                   # build the OpenAPI spec from REST Docs snippets (runs tests first)
./gradlew copyJson                   # openapi3 + copy the spec into src/main/resources/restdocs
./gradlew bootRun                    # run the app on port 8082
./gradlew clean                      # NOTE: also deletes src/main/resources/restdocs/restdoc-swagger-ui-api.json
```

No linter or formatter is configured.

## How the doc pipeline works

1. Controller tests (`@WebMvcTest` + `@AutoConfigureRestDocs`) call endpoints through `RestDocumentationRequestBuilders` and record them with `MockMvcRestDocumentationWrapper.document(<operationId>, ..., resource(ResourceSnippetParameters...))`. The `resource(...)` snippet holds the summary, description, tag, path parameters and request/response field descriptors. See `ExampleControllerTest` for the pattern.
2. `openapi3` (configured in `build.gradle`: server `http://localhost:8082`, JSON format) merges those snippets into `build/api-spec/restdoc-swagger-ui-api.json`. Keep that directory separate from `build/resources/main`: `openapi3` declares no outputs, so when it wrote into `processResources`'s output directory, `copyJson` saw stale file snapshots and was skipped as `NO-SOURCE` after `clean`. Declaring outputs on `openapi3` doesn't work either, because Gradle can't serialize the plugin's `servers` input. The file name comes from `rootProject.name` (`restdoc-swagger-ui`).
3. `copyJson` copies the spec to `src/main/resources/restdocs/`, and **that copy is committed**. After changing an endpoint or its test, run `./gradlew build` and commit the regenerated JSON.

Things to keep in mind when adding or changing endpoints:
- REST Docs fails the test if the response contains a field that is not documented, or if a documented field is missing. The field descriptors must match the full `ApiResult` envelope (`code`, `message`, `value.*`).
- Use `RestDocumentationRequestBuilders` (not plain `MockMvcRequestBuilders`) so that path parameters can be documented.
- Swagger UI comes from `springdoc-openapi-starter-webmvc-ui` (`/swagger-ui/index.html`). `springdoc.swagger-ui.url` in `application.yml` points it at `/restdocs/restdoc-swagger-ui-api.json`, which `global/config/WebConfig` serves from `classpath:/restdocs/`. That explicit resource handler is needed because `@EnableWebMvc` on the main class turns off Boot's static resource auto-configuration. springdoc's own `/v3/api-docs` still exists but is not what the UI shows.
- `processResources` runs before `copyJson`, so a jar from a given `build` contains the spec from the previous build.
- Boot 4 specifics: test annotations live in `org.springframework.boot.webmvc.test.autoconfigure` and `org.springframework.boot.restdocs.test.autoconfigure`; inject `tools.jackson.databind.json.JsonMapper`, not the Jackson 2 `ObjectMapper`. `spring-restdocs-mockmvc` must stay an explicit `testImplementation` because restdocs-api-spec only brings it in at runtime scope.
- The remaining "Task.project at execution time" Gradle deprecation warning comes from the restdocs-api-spec plugin's `openapi3` task, not from `build.gradle`.

## Code layout

- `global/api/ApiResult<T>`: the standard response envelope (`code`, `message`, `value`). By default it uses `ApiStatus.OK`. `getValue()` round-trips `value` through its own Jackson 3 `JsonMapper`, so it serializes as a generic map/object.
- `global/api/ApiStatus`: enum of result codes. The header comment describes a numbering scheme carried over from a larger multi-service system.
- `example/{controller,dto,service}`: the single sample feature. DTOs use Lombok `@Builder` with nested static `SubClass` types.
- `spring-boot-starter-data-redis` and actuator are dependencies, but Redis is not configured.
