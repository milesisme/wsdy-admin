package com.wsdy.saasops.common.aspect;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Slf4j
@Order(4)
@Aspect
@Component
public class TimeAspect {

    @Pointcut("execution(public * com.wsdy.saasops.api.modules.*.controller.*.*(..)) || execution(public * com.wsdy.saasops.modules.*.controller.*.*(..))")
    public void log() {
    }

    //统计请求的处理时间
    ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Before("log()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        startTime.set(System.currentTimeMillis());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        log.info("Aspect_URL:" + request.getRequestURL().toString());
    }

    @AfterReturning(returning = "ret", pointcut = "log()")
    public void doAfterReturning(Object ret) {
        long costTime = System.currentTimeMillis() - startTime.get();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.setHeader("x-resp-time", costTime + "ms");
      //  log.info("方法返回值:" + JSON.toJSONString(ret) + ",方法执行时间:" + costTime);
    }

}
