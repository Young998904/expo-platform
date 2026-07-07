package com.expo.config;

import com.expo.security.CustomerAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 고객 포털(/customer/**)에 경량 인증 인터셉터를 건다. 로그인/로그아웃은 제외.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CustomerAuthInterceptor customerAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(customerAuthInterceptor)
                .addPathPatterns("/customer/**")
                .excludePathPatterns("/customer/login", "/customer/logout");
    }
}
