package com.wsdy.saasops.config;

import com.wsdy.saasops.api.modules.user.service.RedisDelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationRunnerImpl implements ApplicationRunner {

    @Autowired
    private RedisDelService redisDelService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("----spring boot run-----");
        redisDelService.runRedisCache();
    }
}
