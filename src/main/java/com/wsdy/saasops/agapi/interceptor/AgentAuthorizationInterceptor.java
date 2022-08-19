package com.wsdy.saasops.agapi.interceptor;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.service.AgentNewService;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.api.utils.JwtUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Objects.isNull;


@Slf4j
@Component
public class AgentAuthorizationInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private TCpSiteService cpSiteService;
    @Autowired
    private AgentNewService agentNewService;
    @Autowired
    private ApiUserService apiUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String siteCode = CommonUtil.getSiteCode();
        if (StringUtils.isEmpty(siteCode)) {
            throw new R200Exception("站点不存在");
        }
        TCpSite cpSite = cpSiteService.queryPreOneCond(siteCode);
        if (isNull(cpSite)) {
            throw new R200Exception("此站点已不在服务范围");
        }
        AgentLogin annotation;
        if (handler instanceof HandlerMethod) {
            annotation = ((HandlerMethod) handler).getMethodAnnotation(AgentLogin.class);
        } else {
            return true;
        }
        if (isNull(annotation)) {
            return true;
        }
        String token = request.getHeader(jwtUtils.getHeader());
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(jwtUtils.getHeader());
        }
        if (StringUtils.isBlank(token)) {
            throw new R200Exception("token凭证为空", 401);
        }
        Claims claims;
        try {
            log.info("token {}", token);
            claims = jwtUtils.getClaimByToken(token);
        } catch (ExpiredJwtException exception) {
            throw new R200Exception("token失效", 401);
        }
        if (isNull(claims) || jwtUtils.isTokenExpired(claims.getExpiration()) || isNull(claims.getSubject())) {
            throw new R200Exception(jwtUtils.getHeader() + "失效，请重新登录", 401);
        }
        String username = claims.getSubject();
        String[] str = username.split(":");
        
        // 缓存中的token对比
        String tokenCache = apiUserService.queryAgentLoginTokenCache(CommonUtil.getSiteCode(), str[2]);
        if (!StringUtils.equals(tokenCache, token)) {
            throw new R200Exception("token失效,请重新登录", 401);
        }
       
        AgentAccount account = agentNewService.checkoutAvailable(str[2]);
        if (isNull(account)) {
            throw new R200Exception("账户禁止登陆，请联系管理员", 401);
        }
        if (account.getStatus() != 1){
            throw new R200Exception("账户状态不正常，请联系管理员", 401);
        }
        if (account.getStatus() != 1){
            throw new R200Exception("账户状态不正常，请联系管理员", 401);
        }
        request.setAttribute(Constants.AGENT_COOUNT_ID, account);
        return true;
    }
}
