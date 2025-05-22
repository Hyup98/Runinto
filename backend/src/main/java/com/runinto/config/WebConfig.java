package com.runinto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private String resourcePath = "file:///C:/Users/강동협/Desktop/Runinto/backend/uploads/images/";

    private String uploadPath = "/images/**";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(uploadPath) // '/images/'로 시작하는 모든 URL 요청을 처리
                .addResourceLocations(resourcePath); // 해당 요청을 resourcePath 디렉토리에서 찾도록 매핑
    }
}
