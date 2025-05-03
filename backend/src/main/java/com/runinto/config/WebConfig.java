package com.runinto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 외부 디렉토리를 정적 리소스 경로로 매핑하기 위한 경로 (FileUploadController의 UPLOAD_DIR과 일치시키거나, 여기서 관리)
    // 중요: 경로 끝에 '/'를 붙여야 합니다. Windows 경로라도 '/'를 사용하는 것이 좋습니다.
    private String resourcePath = "file:///C:/Users/강동협/Desktop/Runinto/backend/uploads/images/";
    // 웹에서 접근할 URL 경로 패턴 (FileUploadController에서 반환하는 URL과 맞춰야 함)
    private String uploadPath = "/images/**";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(uploadPath) // '/images/'로 시작하는 모든 URL 요청을 처리
                .addResourceLocations(resourcePath); // 해당 요청을 resourcePath 디렉토리에서 찾도록 매핑
    }
}
