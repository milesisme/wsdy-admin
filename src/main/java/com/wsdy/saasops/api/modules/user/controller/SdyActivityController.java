package com.wsdy.saasops.api.modules.user.controller;

import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.user.dto.SdyActivity.RedPacketRainRespDto;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.modules.user.service.SdyActivityService;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.IpUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.lottery.serivce.LotteryActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/api/sdyactivity")
@Api(value = "api", tags = "sdy活动")
public class SdyActivityController {

    @Autowired
    private SdyActivityService sdyActivityService;
    @Autowired
    private LotteryActivityService lotteryActivityService;
    @Autowired
    private RedisService redisService;

    @Login
    @GetMapping(value = "accountSignInfo")
    @ApiOperation(value = "上上签到", notes = "上上签到")
    public R accountSignInfo(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(sdyActivityService.accountSignInfo(accountId));
    }

    @Login
    @GetMapping(value = "completeMaterial")
    @ApiOperation(value = "新手任务-完善资料", notes = "新手任务-完善资料")
    public R completeMaterial(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(sdyActivityService.completeMaterial(accountId));
    }

    @Login
    @GetMapping(value = "firstDeposit")
    @ApiOperation(value = "新手任务-首存送", notes = "新手任务-首存送")
    public R firstDeposit(HttpServletRequest request,
                          @RequestParam(value = "terminal", required = false) Byte terminal) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(sdyActivityService.firstDeposit(accountId, terminal));
    }

    @Login
    @GetMapping(value = "vipInfo")
    @ApiOperation(value = "VIP特权", notes = "VIP特权")
    public R accountVipInfo(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(sdyActivityService.accountVipInfo(accountId));
    }

    @Login
    @GetMapping(value = "accountBirthday")
    @ApiOperation(value = "生日", notes = "生日")
    public R accountBirthday(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(sdyActivityService.accountBirthday(accountId));
    }

    @Login
    @GetMapping(value = "accountVipPrivileges")
    @ApiOperation(value = "VIP晋级优惠查询", notes = "VIP晋级优惠查询")
    public R accountVipPrivileges(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(sdyActivityService.accountVipPrivileges(accountId));
    }

    @GetMapping(value = "lotteryInfoNotLogged")
    @ApiOperation(value = "抽奖查询", notes = "抽奖查询")
    public R lotteryInfoNotLogged() {
        return R.ok().put(lotteryActivityService.lotteryInfo(null,null,null));
    }

    @Login
    @GetMapping(value = "lotteryInfo")
    @ApiOperation(value = "抽奖查询", notes = "抽奖查询")
    public R lotteryInfo(HttpServletRequest request) {
        String mainDomain = IpUtils.getUrl(request);
        Assert.isBlank(mainDomain, "mainDomain不能为空");
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(lotteryActivityService.lotteryInfo(accountId, mainDomain, CommonUtil.getSiteCode()));
    }

    @Login
    @GetMapping(value = "accountLottery")
    @ApiOperation(value = "开始抽奖", notes = "开始抽奖")
    public R accountLottery(HttpServletRequest request,
                            @RequestParam("prizeArea") Integer prizeArea) {
        String mainDomain = IpUtils.getUrl(request);
        Assert.isBlank(mainDomain, "mainDomain不能为空");
        Assert.isNull(prizeArea, "prizeArea不能为空");
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String siteCode = CommonUtil.getSiteCode();
        String key = RedisConstants.ACCOUNT_LOTTERY + siteCode + accountId;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accountId, 10, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(isExpired)) {
            try {
                String ip = CommonUtil.getIpAddress(request);
                return R.ok().put(lotteryActivityService.accountLottery(accountId, mainDomain, prizeArea, siteCode, ip));
            } finally {
                redisService.del(key);
            }
        }
        throw new R200Exception("正在抽奖计算，请稍后");
    }

    @GetMapping(value = "/redPacketRainInfo")
    @ApiOperation(value = "红包雨活动查询", notes = "红包雨活动查询")
    public R redPacketRainInfo(HttpServletRequest request) {
        // 红包雨活动信息查询
        RedPacketRainRespDto ret = sdyActivityService.redPacketRainInfo();
        return R.ok().put(ret);
    }

    @Login
    @GetMapping(value = "redPacketClick")
    @ApiOperation(value = "点击红包", notes = "点击红包")
    public R redPacketClick(HttpServletRequest request) {

        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String ip = CommonUtil.getIpAddress(request);
        String siteCode = CommonUtil.getSiteCode();
        String key = RedisConstants.ACCOUNT_REDPOCKETRAIN_CLICK + siteCode + accountId;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accountId, 200, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            try {
                // 获取红包
                RedPacketRainRespDto ret = sdyActivityService.redPacketClick(accountId,ip);
                return R.ok().put(ret);
            } finally {
                redisService.del(key);
            }
        }
        throw new R200Exception("红包获取中，请稍后");
    }

}
