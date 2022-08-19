package com.wsdy.saasops.common.utils;

import cn.jiguang.common.utils.StringUtils;
import com.wsdy.saasops.api.constants.PayConstants;
import com.wsdy.saasops.api.modules.pay.dto.saaspay.CommonPayResponse;
import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class SPTVUtils {
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private SysSettingService sysSettingService;
    @Value("${sptv.url}")
    private String spliveUrl;

    /**
     * 校验SPTV是否创建用户
     * @param siteCode      站点code
     * @param v2Token       会员登录token
     * @param loginName     登录会员名
     * @param synFlag       是否同步校验标志 true 同步
     */
    public void createUser(String siteCode,String v2Token, String loginName,boolean synFlag){
        log.info("getSptvUser==siteCode==" + siteCode + "==loginName== "+ loginName + "==synFlag" + synFlag + "==v2Token== " + v2Token + "==start" );
        // 查询站点是否配置校验
        SysSetting setting = sysSettingService.getSysSetting(SystemConstants.SPTV_CREATE_USER_FLG);
        if (Objects.isNull(setting) || !"1".equals(setting.getSysvalue())) {    // 1校验，其他不校验
            log.info("getSptvUser==siteCode==" + siteCode  + "==loginName== "+ loginName + "==不开启校验" );
            return ;
        }
        // 入参校验
        if(StringUtil.isEmpty(siteCode) || StringUtil.isEmpty(v2Token) ){
            log.info("getSptvUser==siteCode==" + siteCode  + "==loginName== "+ loginName + "==参数错误" );
            if(synFlag){
                throw new R200Exception("SPTV未知异常:0");
            }
            return;
        }
        // 获得sptvstoken
        String sptvstoken = siteCode + "_" + String.valueOf(System.currentTimeMillis());
        log.info("getSptvUser==siteCode==" + siteCode  + "==loginName== "+ loginName + "==sptvstoken==" + sptvstoken );
        sptvstoken = MsgAES.encryptToString(sptvstoken);
        log.info("getSptvUser==siteCode==" + siteCode  + "==loginName== "+ loginName + "==sptvstoken==" + sptvstoken );

        // 处理请求参数
        Map<String, String> headParams = new HashMap<String, String>(2);
        headParams.put("sptvstoken", sptvstoken);
        headParams.put("token", v2Token);

        String url = spliveUrl + PayConstants.SPTV_CREATE_USER;
        if(!spliveUrl.endsWith("/")) {
            url = spliveUrl + "/" +PayConstants.SPTV_CREATE_USER;
        }

        // 请求
        log.info("getSptvUser==siteCode==" + siteCode + "==loginName== "+ loginName + "==headParams==" +  jsonUtil.toJson(headParams) );
        String result = okHttpService.postJson(okHttpService.getHttpNoProxyClient(), url, null, headParams);
        log.info("getSptvUser==siteCode==" + siteCode  + "==loginName== "+ loginName + "==result==" + result);

        // 处理返回
        if(StringUtils.isEmpty(result)){
            log.info("getSptvUser==siteCode==" + siteCode  + "==loginName== "+ loginName + "==result==" + result);
        }
        CommonPayResponse response = jsonUtil.fromJson(result, CommonPayResponse.class);

        if(Objects.isNull(response)){
            log.info("getSptvUser==siteCode==" + siteCode  + "==loginName== "+ loginName + "==response为空" );
            if(synFlag){
                throw new R200Exception("SPTV未知异常:1");
            }
            return;
        }

        if(Objects.isNull(response.getCode())){
            log.info("getSptvUser==siteCode==" + siteCode  + "==loginName== "+ loginName + "==code为空" );
            if(synFlag){
                throw new R200Exception("SPTV未知异常:2");
            }
            return;
        }

        if(!Integer.valueOf(Constants.EVNumber.zero).equals(response.getCode())){
            log.info("getSptvUser==siteCode==" + siteCode  + "==loginName== "+ loginName + "==code==" +  response.getCode());
            if(synFlag){
                throw new R200Exception("SPTV未知异常:3");
            }
        }

        log.info("getSptvUser==siteCode==" + siteCode + "==loginName== "+ loginName + "==synFlag" + synFlag + "==v2Token== " + v2Token + "==end" );
    }
}
