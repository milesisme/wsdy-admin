package com.wsdy.saasops.sysapi.controller;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.pay.service.PaymentService;
import com.wsdy.saasops.api.modules.user.service.DepotWalletService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.activity.service.FirstChargeOprService;
import com.wsdy.saasops.modules.activity.service.HuPengOprRebateService;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.service.FundWithdrawService;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrActivityLevelMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.AccountRebateCastNewService;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.member.service.AuditCastService;
import com.wsdy.saasops.modules.member.service.MbrWarningService;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import com.wsdy.saasops.modules.operate.service.OprRecMbrService;
import com.wsdy.saasops.modules.operate.service.SetGmGameService;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import com.wsdy.saasops.mt.service.MTDataService;
import com.wsdy.saasops.sysapi.dto.*;
import com.wsdy.saasops.sysapi.service.ApplyExperienceService;
import com.wsdy.saasops.sysapi.service.BatchService;
import com.wsdy.saasops.sysapi.service.DispatcherTaskService;
import com.wsdy.saasops.sysapi.service.RedEnvelopeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_10_DATE;
import static java.util.Objects.isNull;


@Slf4j
@RestController
@RequestMapping("/sysapi")
@Api(value = "提供给batch项目的服务", tags = "提供给batch项目的服务")
public class BatchController {

    @Autowired
    private DepotWalletService depotWalletService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private BatchService batchService;
    @Autowired
    private ApiSysMapper apiSysMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private FundWithdrawService withdrawService;
    @Autowired
    private OprRecMbrService oprRecMbrService;
    @Autowired
    private AuditCastService auditCastService;
    @Autowired
    private OprActActivityService actActivityService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private AccountRebateCastNewService rebateCastNewService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private FundWithdrawService fundWithdrawService;
    @Autowired
    private DispatcherTaskService dispatcherTaskService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private OprActActivityCastService oprActActivityCastService;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private TCpSiteService tCpSiteService;
    @Autowired
    private RedEnvelopeService redEnvelopeService;
    @Autowired
    private SetGmGameService setGmGameService;
    @Autowired
    private MbrActivityLevelMapper activityLevelMapper;
    @Autowired
    private HuPengOprRebateService huPengOprRebateService;
    @Autowired
    private MbrWarningService mbrWarningService;
    @Autowired
    private FirstChargeOprService firstChargeOprService;
    @Autowired
    private MTDataService mtDataService;
    @Autowired
    private ApplyExperienceService applyExperienceService;


    @RequestMapping("/depotBalance")
    @ApiOperation(value = "平台会员余额", notes = "平台会员余额")
    public R depotBalance(@RequestParam("siteCode") String siteCode,
                          @RequestParam("depotId") Integer depotId,
                          @RequestParam("accountId") Integer accountId) {
        Assert.isNull(depotId, "平台Id不能为空");
        Assert.isNull(accountId, "会员Id不能为空");
        Assert.isBlank(siteCode, "siteCode不能为空");
        String cpSiteCode = apiSysMapper.getCpSiteCode(AESUtil.decrypt(siteCode));
        TGmApi gmApi = gmApiService.queryApiObject(depotId, cpSiteCode);
        return R.ok().put(depotWalletService.queryDepotBalance(accountId, gmApi));
    }

    @RequestMapping("/validBetMsg")
    @ApiOperation(value = "会员返水消息通知", notes = "会员返水消息通知")
    public void validBetMsg(@RequestParam("siteCode") String siteCode,
                            @RequestParam("accountId") Integer accountId,
                            @RequestParam("acvitityMoney") BigDecimal acvitityMoney) {
        BizEvent bizEvent = new BizEvent(this, AESUtil.decrypt(siteCode), accountId, BizEventType.MEMBER_COMMISSION_SUCCESS);
        bizEvent.setAcvitityMoney(CommonUtil.adjustScale(acvitityMoney));
        applicationEventPublisher.publishEvent(bizEvent);
    }

    @RequestMapping("/updateMerchantPay")
    @ApiOperation(value = "代付状态更新", notes = "代付状态更新")
    public void updateMerchantPay(@RequestParam("siteCode") String siteCode) {
        log.info("收到BATCH代付状态更新通知【" + AESUtil.decrypt(siteCode) + "】");
        List<AccWithdraw> accWithdraws = fundWithdrawService.fundAccWithdrawMerchant(null);
        if (Collections3.isNotEmpty(accWithdraws)) {
            accWithdraws.stream().forEach(as -> {
                withdrawService.updateMerchantPayment(as, AESUtil.decrypt(siteCode));
            });
        }
    }

    @RequestMapping("/updateOprRecMbr")
    @ApiOperation(value = "已读过期站内消息更新", notes = "代付状态更新")
    public void updateOprRecMbr(@RequestParam("siteCode") String siteCode) {
        log.info("收到BATCH已读过期站内消息更新通知【" + AESUtil.decrypt(siteCode) + "】");
        oprRecMbrService.deleteOprRecMbr();
    }

    @RequestMapping("/auditAccount")
    @ApiOperation(value = "计算会员稽核", notes = "计算会员稽核")
    public void auditAccount(@RequestParam("siteCode") String siteCode) {
        log.info("稽核==siteCode==" + AESUtil.decrypt(siteCode) + "==开始");
        String siteCodes = AESUtil.decrypt(siteCode);
        String siteKey = RedisConstants.AUDIT_ACCOUNT + siteCodes;
        Boolean isSiteExpired = redisService.setRedisExpiredTimeBo(siteKey, siteCodes, 15, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(isSiteExpired)) {
            // 1. 获取站点会员id(存在稽核记录的)
            int count = redisService.findLikeRedis(RedisConstants.AUDIT_ACCOUNT_SIGN + siteCodes + "*");
            if (count == 0) {
                List<Integer> ids = auditCastService.findAuditAccountIds(null);
                // 2. 循环计算会员稽核
                if (Collections3.isNotEmpty(ids)) {
                    List<String> sitePrefix = auditCastService.getSitePrefix(siteCodes);
                    ids.stream().forEach(id -> {
                        auditCastService.doingCronAuditAccount(siteCodes, id, sitePrefix, Boolean.TRUE);
                    });
                }
            }
            redisService.del(siteKey);
        }
        log.info("稽核==siteCode==" + siteCodes + "==结束");
    }

    @RequestMapping("/updateActivityState")
    @ApiOperation(value = "更新活动状态，优惠券状态", notes = "更新活动状态，优惠券状态")
    public void updateActivityState(@RequestParam("siteCode") String siteCode) {
        log.info("收到BATCH开始更新活动状态，优惠券状态【" + AESUtil.decrypt(siteCode) + "】");
        actActivityService.updateActivityStateEx();
    }

    @RequestMapping("/updateDepositAmount")
    @ApiOperation(value = "更新每日存款额度,更新返点显示", notes = "更新每日存款额度,更新返点显示")
    public void updateDepositAmount(@RequestParam("siteCode") String siteCode) {
        log.info("收到BATCH开始更新每日存款额度【" + AESUtil.decrypt(siteCode) + "】");
        batchService.updateDepositAmount();
    }

    @RequestMapping("/updatePayStatus")
    @ApiOperation(value = "更新在线支付状态", notes = "更新在线支付状态")
    public void updatePayStatus(@RequestParam("siteCode") String siteCode) {
        log.info("收到BATCH开始更新在线支付状态【" + AESUtil.decrypt(siteCode) + "】");
        batchService.updateFundDeposit();
        batchService.updateAgentFundDeposit();
        // 查询超过30分钟的订单
        List<FundDeposit> fundDeposits = batchService.getFundDepositList();
        if (Collections3.isNotEmpty(fundDeposits)) {
            String siteCodet = AESUtil.decrypt(siteCode);
            fundDeposits.forEach(fs -> paymentService.getPayResult(fs, siteCodet));
        }
    }

    //@RequestMapping("/accountRebate")
    @ApiOperation(value = "会员返点计算", notes = "会员返点计算")
    public String accountRebate(@RequestParam("siteCode") String siteCode, @RequestParam(value = "clacDay",required = false)String clacDay) {
        String siteCodes = AESUtil.decrypt(siteCode);
        String rt =  rebateCastNewService.friendRebate(siteCodes, clacDay);
        return rt ;
    }

   // @RequestMapping("/accountHuPengRebate")
    @ApiOperation(value = "会员返点计算", notes = "会员返点计算")
    public String accountHuPengRebate(@RequestParam("siteCode") String siteCode, @RequestParam(value = "clacDay",required = false)String clacDay) {
        String siteCodes = AESUtil.decrypt(siteCode);
        String rt =  huPengOprRebateService.accountHuPengRebate(siteCodes, clacDay);
        return rt ;
    }


    // @RequestMapping("/mbrWarning")
    @ApiOperation(value = "会员预警", notes = "会员预警")
    public String mbrWarning(@RequestParam("siteCode") String siteCode, @RequestParam(value = "clacDay",required = false)String clacDay) {
        String siteCodes = AESUtil.decrypt(siteCode);
        String rt =  mbrWarningService.mbrWarning(siteCodes, clacDay);
        return rt ;
    }

    //@RequestMapping("/getReward")
    @ApiOperation(value = "会员预警", notes = "会员预警")
    public R getReward(@RequestParam("accountId")int accountId, @RequestParam("ip")String ip) {
        firstChargeOprService.applyFirstCharge(accountId, ip, "");
        return R.ok();
    }

    //@RequestMapping("/mtCallBack")
    @ApiOperation(value = "会员预警", notes = "会员预警")
    public R mtCallBack(@RequestParam("siteCode") String siteCode, @RequestParam("accountId")int accountId, @RequestParam("ip")String ip) {
        String siteCodes = AESUtil.decrypt(siteCode);
        mtDataService.mtCallBack(siteCodes, accountId,  new BigDecimal(100), "11");
        return R.ok();
    }


    @RequestMapping("/dispatcherTask")
    @ApiOperation(value = "通用执行器", notes = "通用执行器")
    public void dispatcherTask(@RequestParam("siteCode") String siteCode,
                               @RequestParam("taskSign") String taskSig) {
        String siteCodes = AESUtil.decrypt(siteCode);
        String key = RedisConstants.BATCH_DISPATCHERTASK + siteCodes + taskSig;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCodes, 30, TimeUnit.MINUTES);
        log.info("收到BATCH通用执行器请求【" + siteCodes + "】【" + taskSig + "】【"+ key + "】【" +isExpired.toString() +"】");
        if (Boolean.TRUE.equals(isExpired)) {
            dispatcherTaskService.dispatcherTask(siteCodes, taskSig, key);
        }
    }

    @RequestMapping("sptvBonusAudit")
    @ApiOperation(value = "包賠紅包增加稽核", notes = "包賠紅包增加稽核")
    public R sptvBonusAudit(HttpServletRequest request,
                            @ModelAttribute SptvBonusDto dto) {
        Assert.isBlank(dto.getSiteCode(), "siteCode不能为空");
        Assert.isBlank(dto.getBetNumber(), "注单号不能为空");
        Assert.isNull(dto.getBonusAmount(), "红利不能为空");
        log.info(dto.getLoginName() + "包賠紅包增加稽核111111111111" + JSON.toJSONString(dto));
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(dto.getSiteCode()));
        MbrAccount account = new MbrAccount();
        account.setLoginName(dto.getLoginName());
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (isNull(mbrAccount)) {
            throw new R200Exception("会员不存在");
        }
        log.info(mbrAccount.getLoginName() + "包賠紅包增加稽核" + JSON.toJSONString(dto));
        redEnvelopeService.addRedEnvelope(dto, mbrAccount);
        return R.ok();
    }




    @RequestMapping("/getSportName")
    @ApiOperation(value = "查询体育游戏游戏名", notes = "查询体育游戏游戏名")
    public R getSportName(HttpServletRequest request,
    		@ModelAttribute GameNameSportDto dto) {
    	Assert.isBlank(dto.getSiteCode(), "siteCode不能为空");
    	request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(dto.getSiteCode()));
    	return R.ok(setGmGameService.selectSportSetDepotname());
    }


    @PostMapping("/findSubUser")
    @ApiOperation(value = "查找下级账号", notes = "申请体验")
    public ResponseDto findSubUser(HttpServletRequest request,
                                              @RequestBody FindSubUserRequestDto findSubUserRequestDto) {
        Assert.isBlank(findSubUserRequestDto.getSiteCode(), "siteCode不能为空");
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(findSubUserRequestDto.getSiteCode()));
        return applyExperienceService.findSubUser(findSubUserRequestDto);
    }

    @PostMapping("/applyExperience")
    @ApiOperation(value = "申请体验", notes = "申请体验")
    public ResponseDto applyExperience(HttpServletRequest request,
                          @RequestBody ApplyExperienceRequestDto applyExperienceRequestDto) {
        Assert.isBlank(applyExperienceRequestDto.getSiteCode(), "siteCode不能为空");
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(applyExperienceRequestDto.getSiteCode()));
        return applyExperienceService.applyExperience(applyExperienceRequestDto);
    }

    @PostMapping("/startExperience")
    @ApiOperation(value = "开始体验", notes = "开始体验")
    public ResponseDto startExperience(HttpServletRequest request,
                                       @RequestBody StartExperienceRequestDto startExperienceRequestDto) {
        Assert.isBlank(startExperienceRequestDto.getSiteCode(), "siteCode不能为空");
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(startExperienceRequestDto.getSiteCode()));
        return applyExperienceService.startExperience(startExperienceRequestDto);
    }

    @RequestMapping("m8BonusAudit")
    @ApiOperation(value = "M8体验增加稽核", notes = "M8体验增加稽核")
    public ResponseDto m8BonusAudit(HttpServletRequest request,
                          @ModelAttribute SptvBonusDto dto) {
        Assert.isBlank(dto.getSiteCode(), "siteCode不能为空");
        Assert.isNull(dto.getBonusAmount(), "红利不能为空");
        log.info(dto.getLoginName() + "m8BonusAudit==M8体验加稽核" + JSON.toJSONString(dto));
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(dto.getSiteCode()));
        MbrAccount account = new MbrAccount();
        account.setLoginName(dto.getLoginName());
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        ResponseDto responseDto = new ResponseDto();
        if (isNull(mbrAccount)) {
            responseDto.setCode(String.valueOf(Constants.EVNumber.one));
            responseDto.setMsg("用户不存在");
            return responseDto;
        }
        log.info(mbrAccount.getLoginName() + "M8体验增加稽核" + JSON.toJSONString(dto));
        redEnvelopeService.addM8Envelope(dto, mbrAccount);
        return responseDto;
    }

}