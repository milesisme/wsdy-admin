package com.wsdy.saasops.sysapi.controller;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.modules.member.entity.MbrDepotTradeLog;
import com.wsdy.saasops.sysapi.dto.TradeRequestDto;
import com.wsdy.saasops.sysapi.dto.TradeResponseDto;
import com.wsdy.saasops.sysapi.service.DepotTradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.concurrent.TimeUnit;

import static com.wsdy.saasops.sysapi.dto.TradeResponseDto.*;
import static java.util.Objects.isNull;


@Slf4j
@RestController
@RequestMapping("/sysapi")
@Api(value = "提供给gameapi单一钱包", tags = "提供给gameapi单一钱包")
public class DepotTradeController {

    @Autowired
    private DepotTradeService tradeService;
    @Autowired
    private TCpSiteService tCpSiteService;
    @Autowired
    private RedisService redisService;


    @RequestMapping("trade/findBalance")
    @ApiOperation(value = "查询会员余额", notes = "查询会员余额")
    public TradeResponseDto findBalance(@ModelAttribute TradeRequestDto requestDto,
                                        HttpServletRequest request) {
        TradeResponseDto responseDto = new TradeResponseDto();
        setTradeResponseDto(requestDto, responseDto, Constants.EVNumber.one);
        if (!"00".equals(responseDto.getCode())) {
            return responseDto;
        }
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(requestDto.getSiteCode()));
        return tradeService.findAccountBalance(requestDto.getLoginName());
    }

    @RequestMapping("trade/operatingBalance")
    @ApiOperation(value = "操作会员余额", notes = "操作会员余额")
    public TradeResponseDto operatingBalance(@ModelAttribute TradeRequestDto requestDto,
                                             HttpServletRequest request) {
        log.info(requestDto.getLoginName() + "单一钱包操作余额" + requestDto.getOutTradeno() + ",JSON=" + JSON.toJSONString(requestDto));
        TradeResponseDto responseDto = new TradeResponseDto();
        setTradeResponseDto(requestDto, responseDto, Constants.EVNumber.two);
        log.info(requestDto.getLoginName() + "单一钱包操作余额" + requestDto.getOutTradeno() + ",setTradeResponseDto结果=" + JSON.toJSONString(responseDto));
        if (!"00".equals(responseDto.getCode())) {
            return responseDto;
        }
        /*String key = RedisConstants.TRADE_OPERATING_BALANCE + requestDto.getSiteCode() + requestDto.getLoginName();
        Boolean isExpired = Boolean.TRUE;
        if (requestDto.getOpType() == 0) {
            isExpired = redisService.setRedisExpiredTimeBo(key, requestDto.getSiteCode(), 100, TimeUnit.SECONDS);
        }
        try {
            if (Boolean.TRUE.equals(isExpired)) {
                request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(requestDto.getSiteCode()));
                MbrDepotTradeLog tradeLog = tradeService.addMbrDepotTradeLog(requestDto);
                TradeResponseDto fResult = tradeService.operatingBalance(requestDto, tradeLog);
                log.info(requestDto.getLoginName() + "单一钱包操作余额" + requestDto.getOutTradeno() + ",operatingBalance结果=" + JSON.toJSONString(fResult));
                return fResult;
            }
        } finally {
            if (requestDto.getOpType() == 0) {
                redisService.del(key);
            }
        }
        responseDto.setMsg("正在处理，请勿频繁提交");
        responseDto.setCode(ERROR_CODE_99);
        return responseDto;*/
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(requestDto.getSiteCode()));
        MbrDepotTradeLog tradeLog = tradeService.addMbrDepotTradeLog(requestDto);
        TradeResponseDto fResult = tradeService.operatingBalance(requestDto, tradeLog);
        log.info(requestDto.getLoginName() + "单一钱包操作余额" + requestDto.getOutTradeno() + ",operatingBalance结果=" + JSON.toJSONString(fResult));
        return fResult;
    }

    @RequestMapping("trade/findOrderNo")
    @ApiOperation(value = "查询订单交易", notes = "查询订单交易")
    public TradeResponseDto findOrderNo(@ModelAttribute TradeRequestDto requestDto,
                                        HttpServletRequest request) {
        TradeResponseDto responseDto = new TradeResponseDto();
        if (StringUtils.isEmpty(requestDto.getOutTradeno())) {
            responseDto.setMsg("交易号不能为空");
            responseDto.setCode(ERROR_CODE_44);
            return responseDto;
        }
        if (StringUtils.isEmpty(requestDto.getSiteCode())) {
            responseDto.setMsg("siteCode不能为空");
            responseDto.setCode(ERROR_CODE_22);
            return responseDto;
        }
        String key = RedisConstants.TRADE_OPERATING_BALANCE + requestDto.getSiteCode() + requestDto.getLoginName();
        if (redisService.booleanRedis(key)) {
            request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(requestDto.getSiteCode()));
            return tradeService.findOrderNo(requestDto);
        }
        responseDto.setMsg("正在处理，请勿频繁提交");
        responseDto.setCode(ERROR_CODE_99);
        return responseDto;
    }

    private void setTradeResponseDto(TradeRequestDto requestDto, TradeResponseDto responseDto, Integer sign) {
        if (StringUtils.isEmpty(requestDto.getLoginName())) {
            responseDto.setMsg("会员名不能为空");
            responseDto.setCode(ERROR_CODE_33);
        }
        if (StringUtils.isEmpty(requestDto.getSiteCode())) {
            responseDto.setMsg("siteCode不能为空");
            responseDto.setCode(ERROR_CODE_22);
        }
        if (Constants.EVNumber.two == sign) {
            if (StringUtils.isEmpty(requestDto.getOutTradeno())) {
                responseDto.setMsg("交易号不能为空");
                responseDto.setCode(ERROR_CODE_44);
            }
            if (StringUtils.isEmpty(requestDto.getDepotCode())) {
                responseDto.setMsg("操作类型不能为空");
                responseDto.setCode(ERROR_CODE_55);
            }
            if (isNull(requestDto.getAmount())) {
                responseDto.setMsg("金额不正确");
                responseDto.setCode(ERROR_CODE_66);
            }
            if (StringUtils.isEmpty(requestDto.getDepotCode())) {
                responseDto.setMsg("交易号不能为空");
                responseDto.setCode(ERROR_CODE_77);
            }
            if (StringUtils.isEmpty(requestDto.getFinancialcode())) {
                responseDto.setMsg("类别代码不能为空");
                responseDto.setCode(ERROR_CODE_88);
            }
        }
    }

}