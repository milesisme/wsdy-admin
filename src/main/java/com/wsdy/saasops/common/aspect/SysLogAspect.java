package com.wsdy.saasops.common.aspect;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.ElasticSearchConnection;
import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.IpUtils;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.sys.entity.SysOperatioLog;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.nonNull;


@Aspect
@Order(3)
@Component
@Slf4j
public class SysLogAspect {

    @Autowired
    private ElasticSearchConnection elasticSearchConnection;

    private static final String success = "成功";
    private static final String failed = "失败";

    @Pointcut("@annotation(com.wsdy.saasops.common.annotation.SysLog)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result;
        int time = 0;
        try {
            result = point.proceed();
            time = (int) (System.currentTimeMillis() - beginTime);
        } catch (Exception e) {
            saveSysLog(point, time, failed);
            throw e;
        }
        saveSysLog(point, time, success);
        return result;
    }

    private void saveSysLog(ProceedingJoinPoint joinPoint, int time, String status) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SysOperatioLog operatioLog = new SysOperatioLog();
        SysLog sysLog = method.getAnnotation(SysLog.class);
        if (nonNull(sysLog)) {
            operatioLog.setOperatioTitle(sysLog.module());
            operatioLog.setMethodText(sysLog.methodText());
        }
        operatioLog.setTime(time);
        operatioLog.setStatus(status);
        operatioLog.setIp(IpUtils.getIp());
        operatioLog.setSitePrefix(CommonUtil.getSiteCode());
        operatioLog.setOperatioEquipment(IpUtils.getOsName() );
        //TODO
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        operatioLog.setDev(request.getHeader("dev"));
        SysUserEntity userEntity = ((SysUserEntity) SecurityUtils.getSubject().getPrincipal());
        if (nonNull(userEntity)) {
            operatioLog.setUserName(userEntity.getUsername());
        }
        operatioLog.setOperationTime(new Date());

        String methodName = signature.getName();
        operatioLog.setControllerName(joinPoint.getTarget().getClass().getSimpleName());
        operatioLog.setMethod(methodName);

        Object[] args = joinPoint.getArgs();
        if (args.length != 0 && !"captcha".equals(methodName)) {
            String params = JSON.toJSONString(args[0]);
            operatioLog.setOperatioText(params);
        }
        CompletableFuture.runAsync(() -> {
         //   insert(operatioLog);
        });
    }

    public void insert(Object object) {
        try {
            HttpEntity entity = new NStringEntity(JSON.toJSONStringWithDateFormat(object,
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), ContentType.APPLICATION_JSON);
            elasticSearchConnection.restClient.performRequest("POST",
                    "/" +"sys_operatio" + "/" + "sysOperatioLog" + "/"
                            + new SnowFlake().nextId(), Collections.singletonMap("pretty", "true"), entity);
        } catch (Exception e) {
            log.error("error:" + e);
        }
    }
}
