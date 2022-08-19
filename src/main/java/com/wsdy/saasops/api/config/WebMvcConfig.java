package com.wsdy.saasops.api.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.wsdy.saasops.api.interceptor.AuthorizationInterceptor;
import com.wsdy.saasops.api.resolver.LoginUserHandlerMethodArgumentResolver;

/**
 * MVC配置
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;
    @Autowired
    private LoginUserHandlerMethodArgumentResolver loginUserHandlerMethodArgumentResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationInterceptor).addPathPatterns("/api/**").excludePathPatterns("/api/sys/getSiteCode").excludePathPatterns("/api/sys/getSiteurl").
                excludePathPatterns("/api/sys/findSiteCode").excludePathPatterns("/api/redis/*")
        .excludePathPatterns("/api/user/captcha/*","/api/*/verifyEgDepot","/api/channel/*",
                "/api/OnlinePay/pzPay/paiZiCallback","/api/OnlinePay/pzPay/*/tuolyCallback","/api/callback/*",
                "/api/callback/*/*", "/api/juhepay/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(loginUserHandlerMethodArgumentResolver);
    }

//    @Override
//    public void configurePathMatch(PathMatchConfigurer configurer) {
//        AntPathMatcher matcher = new AntPathMatcher();
//        matcher.setCaseSensitive(false);
//        configurer.setPathMatcher(matcher);
//    }

}