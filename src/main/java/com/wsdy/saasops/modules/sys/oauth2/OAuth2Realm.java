package com.wsdy.saasops.modules.sys.oauth2;

import com.google.common.collect.Lists;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.entity.SysUserTokenEntity;
import com.wsdy.saasops.modules.sys.service.ShiroService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 认证
 */
@Component
@Slf4j
public class OAuth2Realm extends AuthorizingRealm {
    @Autowired
    private ShiroService shiroService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    /**
     * 授权(验证权限时调用)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SysUserEntity user = (SysUserEntity) principals.getPrimaryPrincipal();
        Long userId = user.getUserId();

        //用户权限列表
        Set<String> permsSet = shiroService.getUserPermissions(userId);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        for(String list : permsSet){
            info.addStringPermission(list);
        }
        return info;
    }


    /**
     * 认证(登录时调用)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException, R200Exception{
        String accessToken = (String) token.getPrincipal();
        //根据accessToken，查询用户信息
        SysUserTokenEntity tokenEntity = shiroService.queryByToken(accessToken);
        if (tokenEntity == null || tokenEntity.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new IncorrectCredentialsException("token失效，请重新登录1");
        }
        //查询用户信息
        SysUserEntity user = shiroService.queryUser(tokenEntity.getUserId());
        //账号锁定
        if (user.getStatus() == 0) {
            throw new LockedAccountException("账号已被锁定,请联系管理员");
        }

        // 安全密码校验逻辑
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        if (Boolean.TRUE.equals(checkInterface(request))){
            Map<String, Object> map = CommonUtil.getParameterMap(request);
            String securepwd = Objects.isNull(request.getHeader("securepwd"))? "":request.getHeader("securepwd");
            securepwd  = new Sha256Hash(securepwd, user.getSalt()).toHex();
            // 安全密码校验
            if(!securepwd.equals(user.getSecurepwd())){
                throw new R200Exception("securityError");   // 前端401+securityError 判断不用退出只弹窗
            }
        }
        AuthenticationInfo info = new SimpleAuthenticationInfo(user, accessToken, getName());
        return info;
    }

    private Boolean checkInterface(HttpServletRequest request) {
        List<String> interfaceList = Lists.newArrayList("/bkapi/fund/report/auditAdd","/bkapi/fund/report/auditReduce","/bkapi/fund/report/auditUpdateStatus"
                ,"/bkapi/fund/deposit/updateStatus"
            ,"/bkapi/fund/withdraw/updateAccStatus","/bkapi/fund/withdraw/updateAccStatusFinial","/bkapi/fund/withdraw/updateAccStatusPendPass","/bkapi/fund/withdraw/updateAccStatusSucToFail"
            ,"/bkapi/member/mbraccount/exportMbrAccountInfo","/bkapi/member/audit/updateAudit"
            ,"/bkapi/member/audit/clearAudit","/bkapi/operate/activity/activityAudit","/bkapi/operate/activity/activityModifyAmount"
            ,"/bkapi/sys/user/password","/bkapi/sys/user/secpassword","/bkapi/member/mbraccount/pwdUpdate",
                "/bkapi/agent/withdraw/updateAccStatus","/bkapi/agent/withdraw/updateAccStatusFinial",
                "/bkapi/agent/audit/auditAdd","/bkapi/agent/deposit/updateStatus",
                "/bkapi/mbrAgent/rebate/bonusAuditBatch","/bkapi/fund/deposit/updateStatusSucToFail",
                "/bkapi/sys/role/delete", "/bkapi/sys/user/delete");
        if (interfaceList.contains(request.getServletPath())) {
            return true;
        }
        return false;
    }
}
