package com.wsdy.saasops.mt.controller;

import com.wsdy.saasops.aff.service.SdyDataService;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.mt.annotation.MTEncryptionCheck;
import com.wsdy.saasops.mt.dto.MTResponseDto;
import com.wsdy.saasops.mt.service.MTDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/mt/data")
@Api(value = "mt", tags = "mt代理接口")
public class MTDataController {

    @Autowired
    private TCpSiteService tCpSiteService;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private SdyDataService sdyDataService;
    @Autowired
    private MTDataService mtDataService;


    @MTEncryptionCheck
    @GetMapping("register")
    @ApiOperation(value = "会员接口-账号检测", notes = "根据会员账号检测账号是否存在，存在msg为真，不存在msg为假!")
    public MTResponseDto register(@RequestParam("cpNumber") String cpNumber,
                                  @RequestParam(value = "siteCode") String siteCode,
                                  HttpServletRequest request) {
        if(cpNumber == null && cpNumber.length() == 0){
            return MTResponseDto.response(MTResponseDto.ERROR_CODE_11, "手机号码不能为空");
        }
        if(cpNumber.length() != 11 || !cpNumber.startsWith("1")){
            return MTResponseDto.response(MTResponseDto.ERROR_CODE_11, "手机号码格式不正确");
        }
        if(siteCode == null && siteCode.length() == 0){
            return MTResponseDto.response(MTResponseDto.ERROR_CODE_22, "siteCode不能为空");
        }
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(siteCode));
        String ip = CommonUtil.getIpAddress(request);
        return mtDataService.register(cpNumber, ip);
    }


    @MTEncryptionCheck
    @GetMapping("login")
    @ApiOperation(value = "会员接口-账号登录", notes = "根据会员账号检测账号是否存在，存在msg为真，不存在msg为假!")
    public MTResponseDto login(@RequestParam("cpNumber") String cpNumber,
                                  @RequestParam(value = "siteCode") String siteCode,
                                  HttpServletRequest request) {
        if(cpNumber == null && cpNumber.length() == 0){
            return MTResponseDto.response(MTResponseDto.ERROR_CODE_11, "手机号码不能为空");
        }
        if(siteCode == null && siteCode.length() == 0){
            return MTResponseDto.response(MTResponseDto.ERROR_CODE_22, "siteCode不能为空");
        }
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(siteCode));
        String ip = CommonUtil.getIpAddress(request);
        return mtDataService.login(cpNumber, siteCode, ip);
    }

    @MTEncryptionCheck
    @GetMapping("getBalance")
    @ApiOperation(value = "会员接口-查询余额", notes = "根据会员账号检测账号是否存在，存在msg为真，不存在msg为假!")
    public MTResponseDto getBalance(@RequestParam("cpNumber") String cpNumber,
                                  @RequestParam(value = "siteCode") String siteCode,
                                  HttpServletRequest request) {
        Assert.isBlank(cpNumber, "手机号码不能为空");

        if(cpNumber == null && cpNumber.length() == 0){
            return MTResponseDto.response(MTResponseDto.ERROR_CODE_11, "手机号码不能为空");
        }
        if(siteCode == null && siteCode.length() == 0){
            return MTResponseDto.response(MTResponseDto.ERROR_CODE_22, "siteCode不能为空");
        }
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(siteCode));
        return mtDataService.getBalance(cpNumber);
    }



}
