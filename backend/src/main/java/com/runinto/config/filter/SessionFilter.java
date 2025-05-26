package com.runinto.config.filter;

import com.runinto.auth.domain.SessionConst;
import com.runinto.auth.domain.UserSessionDto;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;
import java.io.IOException;

@Component
@Order(1)
public class SessionFilter implements Filter {

    private static final String[] whitelist = {
            "/auth/signin", "/auth/signup",
            "/swagger-ui.html",
            "/swagger-ui/**",       // Swagger UI 리소스
            "/v3/api-docs",         // OpenAPI 명세서 경로 (정확한 경로)
            "/v3/api-docs/**",      // OpenAPI 명세서 하위 경로 (예: /v3/api-docs/swagger-config)
            "/swagger-resources/**",
            "/webjars/**"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        //화이트리스트 체크
        if (isWhitelistedPath(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // 세션 기반 인증 확인
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        Object sessionAttribute = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (!(sessionAttribute instanceof UserSessionDto)) {
            // 예상치 못한 타입이 세션에 저장된 경우의 오류 처리
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid session attribute type");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isWhitelistedPath(String requestURI) {
        for (String pattern : whitelist) {
            if (PatternMatchUtils.simpleMatch(pattern, requestURI)) {
                return true;
            }
        }
        return false;
    }
}