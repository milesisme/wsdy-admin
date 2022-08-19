package com.wsdy.saasops.mt.config;

import com.wsdy.saasops.mt.interceptor.MTAuthorizationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@Configuration
public class MTWebMvcConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private MTAuthorizationInterceptor mtAuthorizationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mtAuthorizationInterceptor).addPathPatterns("/mt/**");
    }
}