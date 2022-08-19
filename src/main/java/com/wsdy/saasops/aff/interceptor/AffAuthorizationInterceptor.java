package com.wsdy.saasops.aff.interceptor;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.aff.annotation.EncryptionCheck;
import com.wsdy.saasops.api.utils.ASCIIUtils;
import com.wsdy.saasops.api.utils.MD5;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class AffAuthorizationInterceptor extends HandlerInterceptorAdapter {

    @Value("${aff.secretKey}")
    private String affSecretKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        EncryptionCheck annotation;
        if (handler instanceof HandlerMethod) {
            annotation = ((HandlerMethod) handler).getMethodAnnotation(EncryptionCheck.class);
        } else {
            return true;
        }
        if (isNull(annotation)) {
            return true;
        }
        Map<String, Object> parameterMap = CommonUtil.getParameterMap(request);
        log.info("代理请求参数【" + JSON.toJSONString(parameterMap) + "】" + affSecretKey);
        if (isNull(parameterMap.get("sign"))) {
            throw new RRException("sign不能为空");
        }
        String requestSign = parameterMap.get("sign").toString();
        parameterMap.remove("sign");
        String sign = MD5.getMD5(ASCIIUtils.getFormatUrl(parameterMap, affSecretKey));
        if (!sign.equals(requestSign)) {
            throw new RRException("签名错误");
        }
        return true;
    }
}
