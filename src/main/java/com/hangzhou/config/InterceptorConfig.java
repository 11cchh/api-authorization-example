package com.hangzhou.config;

import com.hangzhou.interceptor.ApiAuthorizationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器
 * @Author linchenghui
 * @Date 2021/4/13
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        InterceptorRegistration registration = registry.addInterceptor(new ApiAuthorizationInterceptor());
        // 添加拦截器路径
//        registration.addPathPatterns("/api/interceptor");
    }
}
