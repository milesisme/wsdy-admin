package com.wsdy.saasops.api.interceptor;

import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.api.utils.JwtUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.log.service.LogMbrloginService;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
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
import java.util.concurrent.CompletableFuture;

/**
 * 权限(Token)验证
 */
@Slf4j
@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private TCpSiteService cpSiteService;
    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private LogMbrloginService logMbrloginService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String siteCode = CommonUtil.getSiteCode();
        if (StringUtils.isEmpty(siteCode)) {
            throw new R200Exception("网络错误205");//站点不存在
        }
        TCpSite cpSite = cpSiteService.queryPreOneCond(siteCode);
        if (cpSite == null) {
            throw new R200Exception(siteCode + "网络错误201");//此站点已不在服务范围
        }
        if (cpSite.getAvailable().equals(Available.disable)) {
            throw new R200Exception("网络错误202"); // 此站点已停止服务
        }
        String now = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
        if (!StringUtils.isEmpty(cpSite.getStartDate()) && (now.compareTo(cpSite.getStartDate()) == -1)) {
            throw new R200Exception("网络错误203");//此站点已不在服务范围,站点还未有开始服务
        }
        if (!StringUtils.isEmpty(cpSite.getEndDate()) && (now.compareTo(cpSite.getEndDate()) == 1)) {
            throw new R200Exception("网络错误204");//此站点已不在服务范围,已过期请续费
        }
        request.setAttribute(ApiConstants.WEB_SITE_OBJECT, cpSite);
        Login annotation;
        if (handler instanceof HandlerMethod) {
            annotation = ((HandlerMethod) handler).getMethodAnnotation(Login.class);
        } else {
            return true;
        }
        if (annotation == null) {
            return true;
        }
        // 获取用户凭证
        String token = request.getHeader(jwtUtils.getHeader());
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(jwtUtils.getHeader());
        }
        // 凭证为空
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
        if (claims == null) {
            // throw new R200Exception("token失效,请重新登录", 401);
        }

        String userInfo = claims.getSubject();
        String[] urerInfoArr = userInfo.split(ApiConstants.USER_TOKEN_SPLIT);
        String tokenCache = apiUserService.queryLoginTokenCache(cpSite.getSiteCode(), urerInfoArr[1]);
        if (!tokenCache.equals(token)) {
            // throw new R200Exception("token失效,请重新登录", 401);
        }
        apiUserService.updateLoginTokenCacheListener(cpSite.getSiteCode(), urerInfoArr[1]);
        request.setAttribute(ApiConstants.USER_ID, Integer.parseInt(urerInfoArr[0]));
        request.setAttribute(ApiConstants.USER_NAME, urerInfoArr[1]);



        // 异步更新离线时间
        Integer accountId =  Integer.parseInt(urerInfoArr[0]);
        String url =  request.getRequestURI();


        // 登出不用更新离线状态，由登出程序负责
        if(!"/api/user/loginOut".equals(url)){
            if(accountId != null){
                CompletableFuture.runAsync(() -> {
                    ThreadLocalCache.setSiteCodeAsny(siteCode);
                    MbrAccount mbrAccount =  apiUserService.getById(accountId);
                    if(mbrAccount.getIsOnline() == Constants.EVNumber.zero){
                        logMbrloginService.saveLoginLog(mbrAccount);
                    }
                    apiUserService.updateOfflineById(Integer.parseInt(urerInfoArr[0]));
                });
            }
        }


        return true;
    }
}
