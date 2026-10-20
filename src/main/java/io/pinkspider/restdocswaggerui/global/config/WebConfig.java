package io.pinkspider.restdocswaggerui.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // @EnableWebMvc 로 Boot 의 정적 리소스 자동 설정이 꺼져 있으므로
    // restdocs 로 생성한 OpenAPI 스펙을 Swagger UI 가 읽을 수 있도록 직접 노출한다.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/restdocs/**")
            .addResourceLocations("classpath:/restdocs/");
    }
}
