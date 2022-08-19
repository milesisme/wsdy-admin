package com.wsdy.saasops.api.modules.user.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.unity.dto.LoginModel;
import com.wsdy.saasops.api.modules.unity.service.GameDepotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@Transactional
public class CommonService {

    @Autowired
    private GameDepotService gameDepotService;

    /**
     * 登出
     * @param gmApi
     * @param loginName
     * @return
     */
    public Boolean logOut(TGmApi gmApi, String loginName) {
        LoginModel loginModel = new LoginModel();
        loginModel.setDepotId(gmApi.getDepotId());
        loginModel.setDepotName(gmApi.getDepotCode());
        loginModel.setSiteCode(gmApi.getSiteCode());
        loginModel.setUserName(loginName);
        String resultStr = gameDepotService.logout(loginModel);
        log.error("登出请求返回数据：" + resultStr);
        Map resultMaps = (Map) JSON.parse(resultStr);
        Boolean code = Boolean.parseBoolean(resultMaps.get("code").toString());
        return code;
    }
}
