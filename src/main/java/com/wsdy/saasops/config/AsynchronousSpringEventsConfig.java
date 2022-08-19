package com.wsdy.saasops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

/**
 * Created by William on 2018/3/7.
 */
@Configuration
public class AsynchronousSpringEventsConfig {

    @Resource(name = "eventAsyncExecutor")
    private Executor eventAsyncExecutor;

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(eventAsyncExecutor);
        return eventMulticaster;
    }
}
