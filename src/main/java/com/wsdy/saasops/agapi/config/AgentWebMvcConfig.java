package com.wsdy.saasops.agapi.config;

import com.wsdy.saasops.agapi.interceptor.AgentAuthorizationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;



@Configuration
public class AgentWebMvcConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private AgentAuthorizationInterceptor authorizationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationInterceptor).addPathPatterns("/agapi/**")
                .excludePathPatterns("/agapi/getSiteCode",
                        "/agapi/agentCaptcha.jpg", "/agapi/v2/getSiteCode",
                        "/agapi/n2/getSiteCode",
                        "/agapi/n2/jpg",
                        "/agapi/v2/analysis/betDetails/downloadExcel/**");

    }
}