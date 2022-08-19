package com.wsdy.saasops.aff.config;

import com.wsdy.saasops.aff.interceptor.AffAuthorizationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@Configuration
public class AffWebMvcConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private AffAuthorizationInterceptor affAuthorizationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(affAuthorizationInterceptor).addPathPatterns("/aff/**");
    }
}