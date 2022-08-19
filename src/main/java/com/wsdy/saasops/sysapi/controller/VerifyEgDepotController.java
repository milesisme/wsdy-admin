package com.wsdy.saasops.sysapi.controller;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.sysapi.dto.LoginDto;
import com.wsdy.saasops.sysapi.service.VerifyEgDepotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import static java.util.Objects.isNull;


@Slf4j
@RestController
@RequestMapping("/api")
@Api(value = "提供给apigetaway项目的服务", tags = "提供给apigetaway项目的服务")
public class VerifyEgDepotController {
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private VerifyEgDepotService verifyEgDepotService;
    @Autowired
    private TCpSiteService tCpSiteService;

    @RequestMapping(value = "/*/verifyEgDepot")
    @ApiOperation(value = "EG游戏登陆校验", notes = "EG游戏登陆校验")
    public R verifyEgDepot(HttpServletRequest request, @ModelAttribute LoginDto loginDto) {
        log.info("EG游戏登陆校验" + JSON.toJSONString(loginDto));
        Assert.isNull(loginDto.getUserName(), "会员名不能为空");
        Assert.isNull(loginDto.getPassword(), "密码不能为空");
        loginDto.setUserName(loginDto.getUserName().trim().toLowerCase());
        String urlStr = request.getRequestURI();
        log.info("EG游戏登陆校验请求url:" + urlStr);
        String siteCode = urlStr.substring(urlStr.indexOf("/sysapi") + 6, urlStr.indexOf("/verifyEgDepot"));
        if (StringUtils.isEmpty(tCpSiteService.getSchemaName(siteCode))) {
            throw new R200Exception("站点不存在，或禁用");
        }
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(siteCode));
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(loginDto.getUserName());
        if (isNull(mbrAccount)) {
            throw new R200Exception("无此账号,请注册!");
        }
        if (mbrAccount.getLoginPwd().equals(new Sha256Hash(loginDto.getPassword(), mbrAccount.getSalt()).toHex())) {
            verifyEgDepotService.transfermz(request, loginDto, siteCode, mbrAccount);
            return R.ok(Boolean.TRUE, "密码验证成功！");
        }
        return R.ok(Boolean.FALSE, "用户密码不正确 请重新输入！");
    }
}