package com.campus.campus_life.config;

import com.campus.campus_life.interceptor.JwtInterceptor;
import com.campus.campus_life.utils.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;

    public WebConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor(jwtUtil))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/user/send-code",
                        "/user/reset-pwd",

                        "/api/login",

                        "/ai/chat",
                        "/chat/ask",
                        "/test/ai",

                        "/category/list",
                        "/knowledge/list",
                        "/knowledge/search",
                        "/knowledge/hot",
                        "/knowledge/detail",
                        "/knowledge/category",

                        "/feedback/submit",

                        "/memory/conversation/create",
                        "/memory/conversation/list",
                        "/memory/conversation/update-title",
                        "/memory/conversation/delete",
                        "/memory/message/save",
                        "/memory/message/list",

                        "/error"
                );
    }
}